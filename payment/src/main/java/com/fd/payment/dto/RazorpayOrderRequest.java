package com.fd.payment.dto;

public class RazorpayOrderRequest {

    private Long orderId;
    private Double amount;

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

	public RazorpayOrderRequest(Long orderId, Double amount) {
		super();
		this.orderId = orderId;
		this.amount = amount;
	}

	public RazorpayOrderRequest() {
		
	}
    
}
