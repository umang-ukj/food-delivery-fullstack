package com.fd.order.dto;

import com.fd.order.entity.OrderItem;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderItemResponse {

    private String name;
    private int price;
    private int quantity;

    public OrderItemResponse(OrderItem item) {
        this.name = item.getName();
        this.price = item.getPrice();
        this.quantity = item.getQuantity();
    }

    // getters
}
