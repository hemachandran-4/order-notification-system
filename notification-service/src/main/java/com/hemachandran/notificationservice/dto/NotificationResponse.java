package com.hemachandran.notificationservice.dto;

import com.hemachandran.notificationservice.model.Notification;
import com.hemachandran.notificationservice.model.NotificationStatus;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long orderId,
        String recipientEmail,
        String message,
        NotificationStatus status,
        Instant createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(n.getId(), n.getOrderId(), n.getRecipientEmail(),
                n.getMessage(), n.getStatus(), n.getCreatedAt());
    }
}