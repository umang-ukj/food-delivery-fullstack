package com.fd.order.event.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fd.order.entity.DeliveryEvent;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderStatus;
import com.fd.order.repository.OrderRepository;

@Component
public class DeliveryEventConsumer {

    private final OrderRepository orderRepository;

    public DeliveryEventConsumer(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "delivery-events")
    public void handleDeliveryEvent(DeliveryEvent event) {

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        switch (event.getStatus()) {
            case "OUT_FOR_DELIVERY" -> {
                if (order.getStatus() == OrderStatus.PAID) {
                    order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
                }
            }
            case "DELIVERED" -> {
                if (order.getStatus() == OrderStatus.OUT_FOR_DELIVERY) {
                    order.setStatus(OrderStatus.DELIVERED);
                }
            }
            default -> {
                // ignore unknown statuses (forward compatibility)
            }
        }

        orderRepository.save(order);
    }
}
