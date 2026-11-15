package com.hemachandran.notificationservice.exception;

public class EmailDeliveryException extends RuntimeException {
    public EmailDeliveryException(String message) {
        super(message);
    }
}