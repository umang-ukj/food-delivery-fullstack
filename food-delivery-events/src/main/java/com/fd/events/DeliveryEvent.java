package com.fd.events;


public class DeliveryEvent {
    private Long orderId;
    private DeliveryStatus status;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public DeliveryStatus getStatus() {
		return status;
	}
	public void setStatus(DeliveryStatus status) {
		this.status = status;
	}
	public DeliveryEvent(Long orderId, DeliveryStatus status) {
		super();
		this.orderId = orderId;
		this.status = status;
	}
	public DeliveryEvent() {
		super();
	}

    
}
