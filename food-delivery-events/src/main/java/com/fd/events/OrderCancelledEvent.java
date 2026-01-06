package com.fd.events;

public class OrderCancelledEvent extends OrderEvent {
    private Long orderId;

    public OrderCancelledEvent(Long orderId) {
        super(orderId, null, null);
    }

    public Long getOrderId() {
        return orderId;
    }

	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}

	public OrderCancelledEvent() {
		super();
		// TODO Auto-generated constructor stub
	}


}

