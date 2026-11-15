package com.hemachandran.notificationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
public class KafkaConsumerConfig {

    public static final String ORDER_CREATED_DLT = "order-created.DLT";

    // Must have at least as many partitions as order-created (3),
    // because the recoverer publishes to the same partition number.
    @Bean
    public NewTopic orderCreatedDeadLetterTopic() {
        return TopicBuilder.name(ORDER_CREATED_DLT)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (record, ex) -> new TopicPartition(ORDER_CREATED_DLT, record.partition()));

        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10_000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}