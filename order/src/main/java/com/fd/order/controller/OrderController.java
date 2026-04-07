package com.fd.order.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fd.order.dto.AdminComplaintActionRequest;
import com.fd.order.dto.ComplaintResponse;
import com.fd.order.dto.CreateComplaintRequest;
import com.fd.order.dto.CreateOrderRequest;
import com.fd.order.dto.OrderDetailsResponse;
import com.fd.order.dto.OrderResponse;
import com.fd.order.entity.Order;
import com.fd.order.entity.OrderComplaint;
import com.fd.order.service.OrderService;
import com.fd.order.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public Order createOrder(@RequestHeader("Authorization") String authHeader,
    		@RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderRequest request) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token); 
        String userEmail = jwtUtil.extractEmail(token);
        if (!"user".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Admins cannot place orders");
        }

        //return orderService.createOrder(userId, request);
        return orderService.createOrder(userId, userEmail,idempotencyKey, request);
    }

    @GetMapping("/{orderId}")
    public OrderDetailsResponse getOrderById(@PathVariable Long orderId) {
    	Order order = orderService.findById(orderId);
        return new OrderDetailsResponse(order);
    }

    @GetMapping("/user/me")
    public List<OrderResponse> getMyOrders(@RequestHeader("Authorization") String authHeader) {

        String token = authHeader.substring(7);
        Long userId = jwtUtil.extractUserId(token);

        return orderService.findByUserId(userId).stream()
                .map(OrderResponse::new).toList();
    }
    @PostMapping("/{orderId}/cancel")
    public OrderDetailsResponse cancelOrder(@RequestHeader("Authorization") String authHeader,@PathVariable Long orderId) {
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
    
    @PostMapping("/complaints")
    public ComplaintResponse createComplaint(@RequestHeader("Authorization") String authHeader, 
    		@Valid @RequestBody CreateComplaintRequest request) {
        String token = authHeader.substring(7);
        if (!"user".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can create complaints");
        }
        Long userId = jwtUtil.extractUserId(token);

        try {
            OrderComplaint complaint = orderService.createComplaint(userId, request);
            return new ComplaintResponse(complaint);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }
    
    @GetMapping("/complaints/me")
    public List<ComplaintResponse> getMyComplaints(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!"user".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can view their complaints");
        }
        Long userId = jwtUtil.extractUserId(token);
        return orderService.getComplaintsForUser(userId).stream().map(ComplaintResponse::new).toList();
    }

    @GetMapping("/complaints")
    public List<ComplaintResponse> getAllComplaints(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!"admin".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can view all complaints");
        }

        return orderService.getAllComplaints().stream().map(ComplaintResponse::new).toList();
    }
    
    @PostMapping("/complaints/{complaintId}/admin-action")
    public ComplaintResponse adminActionOnComplaint(@RequestHeader("Authorization") String authHeader,
               @PathVariable Long complaintId,@Valid @RequestBody AdminComplaintActionRequest request) {
        String token = authHeader.substring(7);
        if (!"admin".equals(jwtUtil.extractRole(token))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can resolve complaints");
        }

        try {
            OrderComplaint complaint = orderService.adminActionOnComplaint(complaintId, request);
            return new ComplaintResponse(complaint);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

}
