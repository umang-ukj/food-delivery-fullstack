package com.fd.order.dto;

import java.util.List;

public class CreateOrderRequest {

    private Long userId;
    private String restaurantId;
    private double totalAmount;
    private List<OrderItemRequest> items;
    
    public List<OrderItemRequest> getItems() {
		return items;
	}

	public void setItems(List<OrderItemRequest> items) {
		this.items = items;
	}

	public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

	public CreateOrderRequest(Long userId, String restaurantId, double totalAmount, List<OrderItemRequest> items) {
		super();
		this.userId = userId;
		this.restaurantId = restaurantId;
		this.totalAmount = totalAmount;
		this.items = items;
	}

	public CreateOrderRequest() {
		super();
	}
    
}
