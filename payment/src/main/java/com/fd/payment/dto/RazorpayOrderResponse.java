package com.fd.payment.dto;

public class RazorpayOrderResponse {

    private String razorpayOrderId;
    private Integer amount;
    private String currency;

    public RazorpayOrderResponse(String razorpayOrderId, Integer amount, String currency) {
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public Integer getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

	public RazorpayOrderResponse() {
		
	}
    
}
