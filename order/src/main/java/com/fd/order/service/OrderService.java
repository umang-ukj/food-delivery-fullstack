package com.fd.order.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.fd.events.OrderConfirmedEvent;
import com.fd.events.PaymentMethod;
import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderItemRequest;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderItem;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderConfirmedEventProducer;
import com.fd.order.event.producer.OrderEventProducer;
import com.fd.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {
	private static final Logger log =
		    LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository repository;
    private final OrderEventProducer producer;
    private final OrderConfirmedEventProducer orderConfirmedProducer;
    private final EmailService emailService;

    public OrderService(OrderRepository repository,
                        OrderEventProducer producer,OrderConfirmedEventProducer orderConfirmedProducer, EmailService emailService) {
        this.repository = repository;
        this.producer = producer;
        this.orderConfirmedProducer=orderConfirmedProducer;
        this.emailService=emailService;
    }

    public Order createOrder(Long userId,String userEmail, CreateOrderRequest request) {
		/*
		 * PaymentMethod paymentMethod =
		 * PaymentMethod.valueOf(request.getPaymentMethod());
		 */
        Order order = new Order();
        order.setUserId(userId);
        order.setUserEmail(userEmail);
        order.setRestaurantName(request.getRestaurantName());
        order.setRestaurantImageUrl(request.getRestaurantImageUrl());
        order.setRestaurantId(request.getRestaurantId());
        order.setStatus(OrderStatus.CREATED);
        order.setOrderedAt(LocalDateTime.now());
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

     //  Always publish ORDER_CREATED
        producer.publishOrderCreated(saved, request.getPaymentMethod());

        //  CASH → auto-confirm immediately
        if (PaymentMethod.CASH.name().equals(request.getPaymentMethod())) {

            saved.setStatus(OrderStatus.CONFIRMED);
            repository.save(saved);

            OrderConfirmedEvent event = new OrderConfirmedEvent(saved.getId());

            orderConfirmedProducer.publish(event);

            log.info("OrderConfirmedEvent published for CASH orderId={}", saved.getId());
            
        }
        emailService.sendOrderConfirmationEmail(userEmail, saved);
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
	public List<Order> getOrdersForUser(Long userId) {
	    return repository.findByUserIdAndStatusNot(userId,OrderStatus.CANCELLED);
	}

}
