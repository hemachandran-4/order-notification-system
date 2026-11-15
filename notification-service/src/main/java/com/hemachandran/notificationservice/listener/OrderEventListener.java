package com.hemachandran.notificationservice.listener;

import com.hemachandran.notificationservice.event.OrderCreatedEvent;
import com.hemachandran.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "order-created")
    public void handle(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order {}", event.orderId());
        notificationService.sendAndRecord(event);
    }
}