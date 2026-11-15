package com.hemachandran.orderservice.service;

import com.hemachandran.orderservice.config.KafkaTopicConfig;
import com.hemachandran.orderservice.dto.CreateOrderRequest;
import com.hemachandran.orderservice.event.OrderCreatedEvent;
import com.hemachandran.orderservice.exception.OrderNotFoundException;
import com.hemachandran.orderservice.model.Order;
import com.hemachandran.orderservice.model.OrderStatus;
import com.hemachandran.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Transactional
    public Order create(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerEmail(request.customerEmail())
                .total(request.total())
                .status(OrderStatus.CREATED)
                .build();
        Order saved = repository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(
                saved.getId(), saved.getCustomerEmail(), saved.getTotal(), saved.getCreatedAt());

        kafkaTemplate.send(KafkaTopicConfig.ORDER_CREATED_TOPIC, saved.getId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event for order {}", saved.getId(), ex);
                    } else {
                        log.info("Published OrderCreatedEvent for order {} to partition {} offset {}",
                                saved.getId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });

        return saved;
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }
}