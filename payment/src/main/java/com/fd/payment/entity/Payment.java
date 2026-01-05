package com.fd.payment.entity;

import java.time.LocalDateTime;

import com.fd.events.PaymentMethod;
import com.fd.events.PaymentStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "orderId"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          

    private Long orderId;   
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private Double amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
 // Razorpay fields (can be null for COD)
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

