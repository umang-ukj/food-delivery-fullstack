package com.fd.order.entity;

public class DeliveryEvent {
    private Long orderId;
    private String status; // OUT_FOR_DELIVERY | DELIVERED
    
	public DeliveryEvent() {
		super();
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
	public DeliveryEvent(Long orderId, String status) {
		super();
		this.orderId = orderId;
		this.status = status;
	}
    
}

