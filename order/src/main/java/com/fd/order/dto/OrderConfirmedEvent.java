package com.fd.order.dto;

public class OrderConfirmedEvent {
    private Long orderId;

    public OrderConfirmedEvent(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}
