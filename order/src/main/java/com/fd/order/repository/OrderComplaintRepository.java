package com.fd.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fd.order.entity.OrderComplaint;

public interface OrderComplaintRepository extends JpaRepository<OrderComplaint, Long> {
    List<OrderComplaint> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<OrderComplaint> findAllByOrderByCreatedAtDesc();
    Optional<OrderComplaint> findByIdAndUserId(Long id, Long userId);
}
