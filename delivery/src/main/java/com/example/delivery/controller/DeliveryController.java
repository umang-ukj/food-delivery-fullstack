package com.example.delivery.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.delivery.model.DeliveryStatus;
import com.example.delivery.service.DeliveryService;
@RestController
@RequestMapping("/delivery")
public class DeliveryController {
	DeliveryService service;
	
	@PatchMapping("/{orderId}/status")
	public ResponseEntity<Void> updateStatus(
	        @PathVariable Long orderId,
	        @RequestParam DeliveryStatus status) {

	    service.updateStatus(orderId, status);
	    return ResponseEntity.ok().build();
	}

}
