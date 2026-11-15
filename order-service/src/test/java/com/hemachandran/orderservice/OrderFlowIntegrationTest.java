package com.hemachandran.orderservice;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.mysql.MySQLContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class OrderFlowIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer("apache/kafka:3.8.0");

    @LocalServerPort
    int port;

    @Test
    void createOrder_returns201_andPublishesEvent() {
        RestClient client = RestClient.create("http://localhost:" + port);

        var response = client.post().uri("/orders")
                .header("Content-Type", "application/json")
                .body("{\"customerEmail\":\"it@example.com\",\"total\":42.00}")
                .retrieve().toEntity(String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).contains("\"status\":\"CREATED\"");

        assertThat(readTopic("order-created"))
                .anyMatch(json -> json.contains("it@example.com"));
    }

    @Test
    void invalidOrder_returns400() {
        RestClient client = RestClient.create("http://localhost:" + port);

        HttpStatusCode status = client.post().uri("/orders")
                .header("Content-Type", "application/json")
                .body("{\"customerEmail\":\"not-an-email\",\"total\":-5}")
                .exchange((req, res) -> res.getStatusCode());

        assertThat(status.value()).isEqualTo(400);
    }

    private List<String> readTopic(String topic) {
        Map<String, Object> props = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            List<String> values = new java.util.ArrayList<>();
            Instant deadline = Instant.now().plusSeconds(15);
            while (Instant.now().isBefore(deadline) && values.isEmpty()) {
                for (ConsumerRecord<String, String> r : consumer.poll(Duration.ofMillis(500))) {
                    values.add(r.value());
                }
            }
            return values;
        }
    }
}