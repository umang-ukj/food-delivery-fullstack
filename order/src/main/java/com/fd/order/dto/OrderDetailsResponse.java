package com.fd.order.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fd.order.entity.Order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailsResponse {
    private Long id;
    private String restaurantId;
    private String restaurantName;
    private String status;
    private Double totalAmount;
    private List<OrderItemResponse> items;
    private LocalDateTime orderedAt;
    private LocalDateTime deliveredAt;
    
    public OrderDetailsResponse(Order order) {
        this.id = order.getId();
        this.restaurantId = order.getRestaurantId();
        this.restaurantName=order.getRestaurantName();
        this.status = order.getStatus().name();
        this.totalAmount = order.getTotalAmount();
        this.items = order.getItems().stream()
                .map(OrderItemResponse::new)
                .toList();
        this.orderedAt = order.getOrderedAt();
        this.deliveredAt = order.getDeliveredAt();
    }

}
