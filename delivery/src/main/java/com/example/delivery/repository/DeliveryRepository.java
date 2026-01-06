package com.example.delivery.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.delivery.model.Delivery;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long>{

	//Optional<Delivery> findByOrderId(Long orderId);

	Optional<Delivery> findFirstByOrderId(Long orderId);

}
