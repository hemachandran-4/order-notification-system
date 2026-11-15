package com.hemachandran.orderservice.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email")
        String customerEmail,

        @NotNull(message = "total is required")
        @DecimalMin(value = "0.01", message = "total must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "total allows up to 8 digits and 2 decimals")
        BigDecimal total
) {}
