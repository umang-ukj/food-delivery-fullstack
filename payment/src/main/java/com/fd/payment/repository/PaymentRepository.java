package com.fd.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fd.events.PaymentStatus;
import com.fd.payment.entity.Payment;

public interface PaymentRepository  extends JpaRepository<Payment, Long>{

	Optional<Payment> findByOrderId(Long orderId);

	Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

	@Modifying
	@Query("""
	update Payment p
	set p.status = :status
	where p.id = :id
	  and p.status <> :status
	""")
	int updateStatusIfChanged(
	    @Param("id") Long id,
	    @Param("status") PaymentStatus status
	);

}
