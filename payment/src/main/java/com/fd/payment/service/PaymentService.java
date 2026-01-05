package com.fd.payment.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fd.events.OrderEvent;
import com.fd.events.PaymentEvent;
import com.fd.events.PaymentMethod;
import com.fd.events.PaymentStatus;
import com.fd.payment.dto.PaymentRequest;
import com.fd.payment.dto.PaymentResponse;
import com.fd.payment.entity.Payment;
import com.fd.payment.producer.PaymentEventProducer;
import com.fd.payment.repository.PaymentRepository;

@Service
public class PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);

    private final PaymentEventProducer producer;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentEventProducer producer,
                          PaymentRepository paymentRepository) {
        this.producer = producer;
        this.paymentRepository = paymentRepository;
    }

    public void processPayment(OrderEvent event) {
        // delegate to existing logic
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(event.getOrderId());
        request.setMethod(event.getPaymentMethod());

        createPayment(request);
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        log.info("Creating payment for orderId={}, method={}",
                request.getOrderId(), request.getMethod());

        // Idempotency check
        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(p -> {
                    throw new IllegalStateException(
                            "Payment already exists for orderId=" + request.getOrderId());
                });

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getMethod());
        payment.setCreatedAt(LocalDateTime.now());

        // DEFAULT STATE
        payment.setStatus(PaymentStatus.PAYMENT_CREATED);

        // COD shortcut (important)
        if (request.getMethod() == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        }
        else {
        // Online payments (Razorpay)
        	payment.setStatus(PaymentStatus.PAYMENT_INITIATED);
         }

        Payment saved = paymentRepository.save(payment);

        // Publish Kafka event
        PaymentEvent event = new PaymentEvent(
                saved.getOrderId(),
                saved.getStatus(),
                saved.getPaymentMethod()
        );

        producer.publish(event);

        log.info("Payment {} saved and event published with status={}",
                saved.getId(), saved.getStatus());

        return new PaymentResponse(saved);
    }

    /**
     * STEP 2: Query payment by order
     */
    public PaymentResponse getPaymentByOrder(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No payment found for orderId=" + orderId));

        return new PaymentResponse(payment);
    }
}