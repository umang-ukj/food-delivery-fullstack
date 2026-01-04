package com.example.delivery.service;

import org.springframework.stereotype.Service;

import com.example.delivery.model.Delivery;
import com.example.delivery.model.DeliveryStatus;
import com.example.delivery.repository.DeliveryRepository;

import jakarta.transaction.Transactional;

@Service
public class DeliveryService {
	DeliveryRepository deliveryRepository;
	@Transactional
	public void updateStatus(Long orderId, DeliveryStatus status) {
	    Delivery delivery = deliveryRepository.findByOrderId(orderId)
	            .orElseThrow(() -> new RuntimeException("Delivery not found"));

	    delivery.setStatus(status);
	}

}
