package com.hemachandran.notificationservice.service;

import com.hemachandran.notificationservice.event.OrderCreatedEvent;
import com.hemachandran.notificationservice.model.Notification;
import com.hemachandran.notificationservice.model.NotificationStatus;
import com.hemachandran.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public Notification sendAndRecord(OrderCreatedEvent event) {
        String message = "Hi! Your order #%d for %s has been received."
                .formatted(event.orderId(), event.total());

        // Simulated email: in a real system this would call SMTP / SendGrid
        log.info("📧 Sending email to {}: {}", event.customerEmail(), message);

        Notification notification = Notification.builder()
                .orderId(event.orderId())
                .recipientEmail(event.customerEmail())
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        return repository.save(notification);
    }
}