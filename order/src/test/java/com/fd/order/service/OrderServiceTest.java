package com.fd.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fd.events.OrderConfirmedEvent;
import com.fd.events.PaymentMethod;
import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderItemRequest;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.event.producer.OrderConfirmedEventProducer;
import com.fd.order.event.producer.OrderEventProducer;
import com.fd.order.repository.OrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;
    @Mock
    private OrderEventProducer producer;
    @Mock
    private OrderConfirmedEventProducer orderConfirmedProducer;
    @Mock
    private EmailService emailService;

    private OrderService service;

    @BeforeEach
    void setUp() {
        service = new OrderService(repository, producer, orderConfirmedProducer, emailService);
    }

    @Test
    void createOrder_cashPayment_confirmsPublishesAndEmails() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setRestaurantId("r-1");
        request.setRestaurantName("Demo Bistro");
        request.setRestaurantImageUrl("img");
        request.setPaymentMethod(PaymentMethod.CASH);
        request.setItems(List.of(new OrderItemRequest("i1", "Burger", 100, 2),new OrderItemRequest("i2", "Fries", 50, 1)));

        Order initialSave = new Order();
        initialSave.setId(11L);
        initialSave.setStatus(OrderStatus.CREATED);

        Order confirmedSave = new Order();
        confirmedSave.setId(11L);
        confirmedSave.setStatus(OrderStatus.CONFIRMED);

        when(repository.findByUserIdAndIdempotencyKey(7L, "idem-1")).thenReturn(Optional.empty());
        when(repository.save(any(Order.class))).thenReturn(initialSave, confirmedSave);

        Order result = service.createOrder(7L, "user@example.com", "idem-1", request);

        ArgumentCaptor<Order> createdOrderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(repository).save(createdOrderCaptor.capture());
        Order created = createdOrderCaptor.getValue();

        assertEquals(250.0, created.getTotalAmount());
        assertEquals(OrderStatus.CREATED, created.getStatus());
        assertEquals(2, created.getItems().size());
        assertEquals("Demo Bistro", created.getRestaurantName());

        verify(producer).publishOrderCreated(initialSave, PaymentMethod.CASH);
        verify(orderConfirmedProducer).publish(any(OrderConfirmedEvent.class));
        verify(emailService).sendOrderConfirmationEmail("user@example.com", confirmedSave);
        assertSame(confirmedSave, result);
    }

    @Test
    void createOrder_withExistingIdempotency_returnsExistingWithoutSideEffects() {
        Order existing = new Order();
        existing.setId(22L);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentMethod(PaymentMethod.UPI);

        when(repository.findByUserIdAndIdempotencyKey(5L, "same-key")).thenReturn(Optional.of(existing));

        Order result = service.createOrder(5L, "user@example.com", "same-key", request);

        assertSame(existing, result);
        verify(repository, never()).save(any(Order.class));
        verify(producer, never()).publishOrderCreated(any(Order.class), any(PaymentMethod.class));
        verify(orderConfirmedProducer, never()).publish(any(OrderConfirmedEvent.class));
        verify(emailService, never()).sendOrderConfirmationEmail(eq("user@example.com"), any(Order.class));
    }
}
