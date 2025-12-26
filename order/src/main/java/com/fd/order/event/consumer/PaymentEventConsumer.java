package com.fd.order.event.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.entity.PaymentEvent;
import com.fd.order.repository.OrderRepository;

@Component
public class PaymentEventConsumer {

	
    private final OrderRepository repository = null;

    @KafkaListener(topics = "payment-events")
    public void handlePaymentEvent(PaymentEvent event) {
        Order order = repository.findById(event.getOrderId())
                .orElseThrow();

        if ("SUCCESS".equals(event.getStatus())) {
            order.setStatus(OrderStatus.PAID);
        } else {
            order.setStatus(OrderStatus.FAILED);
        }

        repository.save(order);
    }
}

