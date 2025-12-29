package com.fd.payment.model;


//incoming event from order service-kafka async communication
public class OrderEvent {
    private Long orderId;
    private Double amount;
    private String status;
	public Long getOrderId() {
		return orderId;
	}
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "OrderEvent [orderId=" + orderId + ", amount=" + amount + ", status=" + status + "]";
	}
	public OrderEvent(Long orderId, Double amount, String status) {
		super();
		this.orderId = orderId;
		this.amount = amount;
		this.status = status;
	}
	public OrderEvent() {
		super();
	}
    
}
