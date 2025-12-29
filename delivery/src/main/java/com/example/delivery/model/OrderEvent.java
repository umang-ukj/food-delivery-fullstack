package com.example.delivery.model;

public class OrderEvent {
    private Long orderId;
    private String status; // PAID
    
	public OrderEvent() {
		super();
	}
	public OrderEvent(Long orderId, String status) {
		super();
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

