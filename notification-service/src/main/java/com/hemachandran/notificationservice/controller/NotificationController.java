package com.hemachandran.notificationservice.controller;

import com.hemachandran.notificationservice.dto.NotificationResponse;
import com.hemachandran.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public List<NotificationResponse> list(@RequestParam(required = false) Long orderId) {
        return service.findAll(orderId).stream().map(NotificationResponse::from).toList();
    }

    @GetMapping("/{id}")
    public NotificationResponse get(@PathVariable Long id) {
        return NotificationResponse.from(service.getById(id));
    }
}