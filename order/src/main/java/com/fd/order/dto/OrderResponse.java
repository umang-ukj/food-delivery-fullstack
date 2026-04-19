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
public class OrderResponse {

    private Long id;
    private String restaurantId;
    private String status;
    private String restaurantName;
    private Double totalAmount;
    private LocalDateTime orderedAt;
    private LocalDateTime deliveredAt;
    private Long userId;
    private String userEmail;
    
    public OrderResponse(Order order) {
        this.id = order.getId();
        this.restaurantId = order.getRestaurantId();
        this.restaurantName=order.getRestaurantName();
        this.status = order.getStatus().name();
        this.totalAmount = order.getTotalAmount();
        this.orderedAt = order.getOrderedAt();
        this.deliveredAt = order.getDeliveredAt();
        this.userId=order.getUserId();
        this.userEmail=order.getUserEmail();
    }

}
