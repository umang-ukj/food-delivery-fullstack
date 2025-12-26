package com.fd.order.service;

import org.springframework.stereotype.Service;

import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderEventProducer;
import com.fd.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository repository;
    private final OrderEventProducer producer;

    public OrderService(OrderRepository repository,
                        OrderEventProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.CREATED);
        Order saved = repository.save(order);

        producer.publishOrderCreated(saved);

        return saved;
    }
}
