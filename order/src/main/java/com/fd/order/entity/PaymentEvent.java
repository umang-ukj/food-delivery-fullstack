package com.fd.order.entity;

public class PaymentEvent {

    private Long orderId;
    private String status; // SUCCESS | FAILED

    public PaymentEvent() {
    }

    public PaymentEvent(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
