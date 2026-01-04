package com.fd.events;
public class OrderEvent {
    private Long orderId;
    private Double amount;
    //private String status;
    private String paymentMethod; //cod/card/upi
    
	public String getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
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
	/*
	 * public String getStatus() { return status; } public void setStatus(String
	 * status) { this.status = status; }
	 */
	
	public OrderEvent(Long orderId, Double amount, String paymentMethod) {
		super();
		this.orderId = orderId;
		this.amount = amount;
		//this.status = status;
		this.paymentMethod=paymentMethod;
	}
	public OrderEvent() {
		super();
	}
	
}
