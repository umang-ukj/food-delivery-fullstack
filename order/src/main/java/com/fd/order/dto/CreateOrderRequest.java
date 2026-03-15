package com.fd.order.dto;

import java.util.List;

import com.fd.events.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {

    private String restaurantId;
    private List<OrderItemRequest> items;
    private PaymentMethod paymentMethod;
    private String restaurantName;
    private String restaurantImageUrl;
    
    public List<OrderItemRequest> getItems() {
		return items;
	}

	public void setItems(List<OrderItemRequest> items) {
		this.items = items;
	}

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public String getRestaurantImageUrl() {
		return restaurantImageUrl;
	}

	public void setRestaurantImageUrl(String restaurantImageUrl) {
		this.restaurantImageUrl = restaurantImageUrl;
	}

}
