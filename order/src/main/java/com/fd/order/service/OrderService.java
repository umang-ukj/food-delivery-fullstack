package com.fd.order.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.fd.events.OrderConfirmedEvent;
import com.fd.events.PaymentMethod;
import com.fd.order.dto.AdminComplaintActionRequest;
import com.fd.order.dto.CreateComplaintRequest;
import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderItemRequest;
import com.fd.order.entity.ComplaintStatus;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderComplaint;
import com.fd.order.entity.OrderItem;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderConfirmedEventProducer;
import com.fd.order.event.producer.OrderEventProducer;
import com.fd.order.repository.OrderComplaintRepository;
import com.fd.order.repository.OrderRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderService {
	private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository repository;
    private final OrderEventProducer producer;
    private final OrderComplaintRepository complaintRepository;
    private final OrderConfirmedEventProducer orderConfirmedProducer;
    private final EmailService emailService;
    private static final EnumSet<OrderStatus> CANCELLABLE_STATUSES =EnumSet.of(OrderStatus.CREATED, OrderStatus.CONFIRMED, OrderStatus.PICKED_UP);
    public OrderService(OrderRepository repository, OrderComplaintRepository complaintRepository, OrderEventProducer producer,OrderConfirmedEventProducer orderConfirmedProducer, EmailService emailService) {
        this.repository = repository;
        this.producer = producer;
        this.complaintRepository = complaintRepository;
        this.orderConfirmedProducer=orderConfirmedProducer;
        this.emailService=emailService;
    }

    public Order createOrder(Long userId, String userEmail, String idempotencyKey, CreateOrderRequest request) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<Order> existing = repository.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
            if (existing.isPresent()) {
                log.info("Returning existing order {} for user {} with idempotency key {}", existing.get().getId(), userId, idempotencyKey);
                return existing.get();
            }
        }
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
        order.setIdempotencyKey(idempotencyKey);
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
	public Order cancelOrder(Long userId, Long orderId) {
        Order order = repository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only cancel your own order");
        }
        if (!CANCELLABLE_STATUSES.contains(order.getStatus())) {
            throw new IllegalStateException("Order cannot be cancelled once it is out for delivery/delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = repository.save(order);
        producer.publishOrderCancelled(orderId);
        log.info("Order {} cancelled by user {}", orderId, userId);
        return saved;
    }
	
	public OrderComplaint createComplaint(Long userId, CreateComplaintRequest request) {
        Order order = repository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("You can only raise complaints for your own orders");
        }
        if (complaintRepository.existsByOrderIdAndUserId(order.getId(), userId)) {
            throw new IllegalStateException("Only one complaint is allowed per order");
        }

        OrderComplaint complaint = new OrderComplaint();
        complaint.setOrderId(order.getId());
        complaint.setUserId(userId);
        complaint.setSubject(request.getSubject());
        complaint.setDescription(request.getDescription());
        complaint.setStatus(ComplaintStatus.OPEN);

        return complaintRepository.save(complaint);
    }
	
	public List<OrderComplaint> getComplaintsForUser(Long userId) {
        return complaintRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<OrderComplaint> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    public OrderComplaint adminActionOnComplaint(Long complaintId, AdminComplaintActionRequest request) {
        OrderComplaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        if (ComplaintStatus.CLOSED.equals(complaint.getStatus())) {
            throw new IllegalStateException("Closed complaints cannot be edited");
        }
        complaint.setStatus(request.getStatus());
        complaint.setAdminResponse(request.getAdminResponse());

        if (request.isInitiateRefund()) {
            complaint.setStatus(ComplaintStatus.REFUND_INITIATED);
            complaint.setRefundRequestedAt(LocalDateTime.now());
            producer.publishOrderCancelled(complaint.getOrderId());
            log.info("Refund initiated from complaintId={} for orderId={}", complaintId, complaint.getOrderId());
        }

        return complaintRepository.save(complaint);
    }
    
    public Page<Order> getAllOrdersForAdmin(Long userId, String restaurantId, OrderStatus status, LocalDateTime orderedFrom, LocalDateTime orderedTo, Pageable pageable) {
        return repository.findAllForAdmin(userId, restaurantId, status, orderedFrom, orderedTo, pageable);
}

}
