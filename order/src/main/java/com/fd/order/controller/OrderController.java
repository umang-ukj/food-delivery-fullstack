package com.fd.order.controller;

import java.util.List;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderItemRequest;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderItem;
import com.fd.order.entity.OrderStatus;
import com.fd.order.service.OrderService;
import com.fd.order.util.JwtUtil;

import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // CREATE ORDER (secure, DTO-based)
    @PostMapping
    public Order createOrder(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateOrderRequest request) {

        String token = authHeader.substring(7); // Bearer
        Long userId = JwtUtil.extractUserId(token);

        return orderService.createOrder(userId, request);
    }


    // GET ORDER BY ID
    @GetMapping("/{orderId}")
    public Order getOrderById(@PathVariable Long orderId) {
        return orderService.findById(orderId);
    }

    // GET CURRENT USER ORDERS
    @GetMapping("/user/me")
    public List<Order> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = JwtUtil.extractUserId(token);

        return orderService.findByUserId(userId);
    }

}
