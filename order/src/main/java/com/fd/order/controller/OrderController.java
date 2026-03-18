package com.fd.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderDetailsResponse;
import com.fd.order.dto.OrderResponse;
import com.fd.order.entity.Order;
import com.fd.order.service.OrderService;
import com.fd.order.util.JwtUtil;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Order createOrder(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateOrderRequest request) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token); 
        String userEmail = jwtUtil.extractEmail(token);
        if (!"user".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admins cannot place orders"
            );
        }

        //return orderService.createOrder(userId, request);
        return orderService.createOrder(userId, userEmail, request);
    }

    @GetMapping("/{orderId}")
    public OrderDetailsResponse getOrderById(@PathVariable Long orderId) {
    	Order order = orderService.findById(orderId);
        return new OrderDetailsResponse(order);
    }

    @GetMapping("/user/me")
    public List<OrderResponse> getMyOrders(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        return orderService.findByUserId(userId).stream()
                .map(OrderResponse::new)
                .toList();
    }
    @PostMapping("/{orderId}/cancel")
    public OrderDetailsResponse cancelOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId) {
        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);
        if (!"user".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins cannot cancel orders");
        }

        try {
            Order order = orderService.cancelOrder(userId, orderId);
            return new OrderDetailsResponse(order);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        }
    }
}
