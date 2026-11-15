package com.hemachandran.orderservice.service;

import com.hemachandran.orderservice.dto.CreateOrderRequest;
import com.hemachandran.orderservice.exception.OrderNotFoundException;
import com.hemachandran.orderservice.model.Order;
import com.hemachandran.orderservice.model.OrderStatus;
import com.hemachandran.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;

    @Transactional
    public Order create(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerEmail(request.customerEmail())
                .total(request.total())
                .status(OrderStatus.CREATED)
                .build();
        return repository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}