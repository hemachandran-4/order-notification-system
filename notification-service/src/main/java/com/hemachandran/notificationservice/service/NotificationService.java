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

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repository;

    @Transactional
    public void sendAndRecord(OrderCreatedEvent event) {
        // Layer 1: fast path for the common duplicate case
        if (repository.existsByOrderId(event.orderId())) {
            log.warn("Duplicate event for order {}, skipping", event.orderId());
            return;
        }

        String message = "Hi! Your order #%d for %s has been received."
                .formatted(event.orderId(), event.total());

        if (event.customerEmail().endsWith("@fail.com")) {
            log.warn("Email delivery failed for order {} ({})", event.orderId(), event.customerEmail());
            throw new EmailDeliveryException("Simulated SMTP failure for " + event.customerEmail());
        }

        log.info("📧 Sending email to {}: {}", event.customerEmail(), message);

        Notification notification = Notification.builder()
                .orderId(event.orderId())
                .recipientEmail(event.customerEmail())
                .message(message)
                .status(NotificationStatus.SENT)
                .build();

        // Layer 2: the unique constraint is the final guard against races
        try {
            repository.saveAndFlush(notification);
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event for order {} caught by unique constraint, skipping", event.orderId());
        }
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