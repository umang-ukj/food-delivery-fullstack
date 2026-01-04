package com.fd.order.service;

import java.util.List;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.backoff.FixedBackOff;

import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderItemRequest;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderItem;
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

    public Order createOrder(Long userId, CreateOrderRequest request) {

        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(request.getRestaurantId());
        order.setStatus(OrderStatus.CREATED);

        order.setItems(new java.util.ArrayList<>());
        double total = 0;

        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setItemId(itemReq.getItemId());
            item.setName(itemReq.getName());
            item.setPrice(itemReq.getPrice());
            item.setQuantity(itemReq.getQuantity());
            item.setOrder(order);
            
            order.getItems().add(item);
            total += itemReq.getPrice() * itemReq.getQuantity();
        }

        order.setTotalAmount(total);
        Order saved = repository.save(order);

        producer.publishOrderCreated(saved, request.getPaymentMethod());

        return saved;
    }
    
    public List<Order> findByUserId(Long userId) {
        List<Order> orders = repository.findByUserId(userId);

        if (orders.isEmpty()) {
            throw new RuntimeException("No orders found for user: " + userId);
        }

        return orders;
    }

	public Order findById(Long orderId) {
		return repository.findByIdWithItems(orderId).orElseThrow(()->new RuntimeException("order not found"));
	}

	public Order save(Order order) {
		return repository.save(order);
	}


}
