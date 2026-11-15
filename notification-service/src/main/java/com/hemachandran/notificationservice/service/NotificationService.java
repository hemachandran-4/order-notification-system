package com.hemachandran.notificationservice.service;

import com.hemachandran.notificationservice.event.OrderCreatedEvent;
import com.hemachandran.notificationservice.exception.EmailDeliveryException;
import com.hemachandran.notificationservice.exception.NotificationNotFoundException;
import com.hemachandran.notificationservice.model.Notification;
import com.hemachandran.notificationservice.model.NotificationStatus;
import com.hemachandran.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Sort;
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

        if (event.customerEmail().endsWith("@fail.com")) {
            log.warn("Email delivery failed for order {} ({})", event.orderId(), event.customerEmail());
            throw new EmailDeliveryException("Simulated SMTP failure for " + event.customerEmail());
        }
        
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

    @Transactional(readOnly = true)
    public List<Notification> findAll(Long orderId) {
        if (orderId != null) {
            return repository.findByOrderId(orderId);
        }
        return repository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Notification getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotificationNotFoundException(id));
    }
}