package com.hemachandran.orderservice.dto;

import com.hemachandran.orderservice.model.Order;
import com.hemachandran.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String customerEmail,
        BigDecimal total,
        OrderStatus status,
        Instant createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getCustomerEmail(), o.getTotal(),
                o.getStatus(), o.getCreatedAt());
    }
}