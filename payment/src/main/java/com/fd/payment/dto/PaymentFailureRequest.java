package com.fd.payment.dto;

public class PaymentFailureRequest {

    private Long orderId;
    private String razorpayOrderId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

	public PaymentFailureRequest(Long orderId, String razorpayOrderId) {
		super();
		this.orderId = orderId;
		this.razorpayOrderId = razorpayOrderId;
	}
}
