package com.hemachandran.notificationservice;

import com.hemachandran.notificationservice.repository.NotificationRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.TopicBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@Import(NotificationFlowIntegrationTest.TopicConfig.class)
class NotificationFlowIntegrationTest {

    @TestConfiguration
    static class TopicConfig {
        @Bean
        NewTopic orderCreatedTopic() {
            return TopicBuilder.name("order-created").partitions(1).replicas(1).build();
        }
    }

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    @Autowired
    NotificationRepository repository;

    @Test
    void event_isConsumed_andNotificationStored() {
        send("{\"orderId\":101,\"customerEmail\":\"a@example.com\",\"total\":10.00,\"createdAt\":\"2026-11-15T10:00:00Z\"}");

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(repository.findByOrderId(101L)).hasSize(1));
    }

    @Test
    void duplicateEvent_createsOnlyOneNotification() {
        String json = "{\"orderId\":102,\"customerEmail\":\"b@example.com\",\"total\":20.00,\"createdAt\":\"2026-11-15T10:00:00Z\"}";
        send(json);
        send(json);

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(repository.findByOrderId(102L)).hasSize(1));
        // give the duplicate time to be processed, then confirm it is still one
        await().pollDelay(Duration.ofSeconds(3)).atMost(Duration.ofSeconds(10)).untilAsserted(() ->
                assertThat(repository.findByOrderId(102L)).hasSize(1));
    }

    @Test
    void failingEvent_goesToDeadLetterTopic_andIsNotStored() {
        send("{\"orderId\":103,\"customerEmail\":\"c@fail.com\",\"total\":5.00,\"createdAt\":\"2026-11-15T10:00:00Z\"}");

        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of("order-created.DLT"));
            await().atMost(Duration.ofSeconds(40)).untilAsserted(() -> {
                var records = consumer.poll(Duration.ofMillis(500));
                boolean found = false;
                for (var r : records) {
                    if (r.value().contains("c@fail.com")) found = true;
                }
                assertThat(found).isTrue();
            });
        }
        assertThat(repository.findByOrderId(103L)).isEmpty();
    }

    private void send(String json) {
        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>("order-created", json));
            producer.flush();
        }
    }
}