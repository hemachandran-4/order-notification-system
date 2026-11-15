package com.hemachandran.notificationservice.repository;

import com.hemachandran.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByOrderId(Long orderId);
    List<Notification> findByOrderId(Long orderId);
}