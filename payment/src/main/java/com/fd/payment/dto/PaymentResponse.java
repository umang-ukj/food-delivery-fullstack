package com.fd.payment.dto;

import com.fd.events.PaymentMethod;
import com.fd.events.PaymentStatus;
import com.fd.payment.entity.Payment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PaymentResponse {

    private Long orderId;
    private PaymentStatus status;
    private PaymentMethod method;

    public PaymentResponse(Payment payment) {
        this.orderId = payment.getOrderId();
        this.status = payment.getStatus();
        this.method = payment.getPaymentMethod();
    }
}
