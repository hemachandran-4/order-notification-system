package com.hemachandran.notificationservice.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        Long orderId,
        String customerEmail,
        BigDecimal total,
        Instant createdAt
) {}