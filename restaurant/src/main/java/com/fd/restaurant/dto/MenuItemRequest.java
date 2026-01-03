package com.fd.restaurant.dto;

public class MenuItemRequest {
	private Double price;
    private Boolean available;
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public Boolean getAvailable() {
		return available;
	}
	public void setAvailable(Boolean available) {
		this.available = available;
	}
	public MenuItemRequest(Double price, Boolean available) {
		super();
		this.price = price;
		this.available = available;
	}
	public MenuItemRequest() {
		
	}
    
}
