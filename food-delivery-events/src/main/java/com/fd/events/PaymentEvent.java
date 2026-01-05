package com.fd.events;

public class PaymentEvent {

    private Long orderId;
    private PaymentStatus status;
    private PaymentMethod method;

    public PaymentEvent() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public PaymentMethod getMethod() {
		return method;
	}

	public void setMethod(PaymentMethod method) {
		this.method = method;
	}

	public PaymentEvent(Long orderId, PaymentStatus status, PaymentMethod method) {
		super();
		this.orderId = orderId;
		this.status = status;
		this.method = method;
	}

}