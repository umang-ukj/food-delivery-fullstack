package com.fd.payment.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fd.events.OrderEvent;
import com.fd.events.PaymentEvent;
import com.fd.events.PaymentMethod;
import com.fd.events.PaymentStatus;
import com.fd.payment.config.RazorpayConfig;
import com.fd.payment.dto.PaymentRequest;
import com.fd.payment.dto.PaymentResponse;
import com.fd.payment.dto.RazorpayOrderResponse;
import com.fd.payment.dto.RazorpayVerifyRequest;
import com.fd.payment.entity.Payment;
import com.fd.payment.producer.PaymentEventProducer;
import com.fd.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.Utils;

@Service
public class PaymentService {

	@Value("${razorpay.key.secret}")
    private String razorpaySecret;
    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);
    private final RazorpayConfig razorpayConfig;
    private final PaymentEventProducer producer;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentEventProducer producer,
                          PaymentRepository paymentRepository,RazorpayConfig razorpayConfig) {
        this.producer = producer;
        this.paymentRepository = paymentRepository;
        this.razorpayConfig=razorpayConfig;
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

        // 🔁 Idempotency check
        Optional<Payment> existing =
                paymentRepository.findByOrderId(request.getOrderId());

        if (existing.isPresent()) {
            Payment payment = existing.get();
         // Re-publish event ONLY if status is terminal
            if (payment.getStatus() == PaymentStatus.PAYMENT_SUCCESS ||
                payment.getStatus() == PaymentStatus.PAYMENT_FAILED) {

                producer.publish(payment);
            }

            log.info("Payment already exists for orderId={}, returning existing payment",
                    request.getOrderId());

            return new PaymentResponse(
                    payment.getId(),
                    payment.getStatus(),
                    payment.getPaymentMethod()
            );
        }

        // 🆕 Create new payment
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getMethod());
        payment.setCreatedAt(LocalDateTime.now());

        // Default state
        payment.setStatus(PaymentStatus.PAYMENT_CREATED);

        // COD shortcut
        if (request.getMethod() == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.PAYMENT_INITIATED);
        }

        Payment saved = paymentRepository.save(payment);

        // 📤 Publish Kafka event ONCE
        producer.publish(saved);

        log.info("Payment {} saved and event published with status={}",
                saved.getId(), saved.getStatus());

        return new PaymentResponse(saved);
    }


    public PaymentResponse getPaymentByOrder(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No payment found for orderId=" + orderId));

        return new PaymentResponse(payment);
    }
    @Transactional
    public RazorpayOrderResponse createRazorpayOrder(Long orderId, Double amount) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for orderId " + orderId));

        // Idempotency: Razorpay order already created
        if (payment.getRazorpayOrderId() != null) {
            return new RazorpayOrderResponse(
                    payment.getRazorpayOrderId(),
                    (int) (payment.getAmount() * 100),
                    "INR"
            );
        }

        try {
            JSONObject options = new JSONObject();
            options.put("amount", (int) (amount * 100));
            options.put("currency", "INR");
            options.put("receipt", "order_" + orderId);

            Order razorpayOrder =
                    razorpayConfig.razorpayClient().orders.create(options);

            // UPDATE existing payment
            payment.setAmount(amount);
            payment.setStatus(PaymentStatus.PAYMENT_INITIATED);
            payment.setRazorpayOrderId(razorpayOrder.get("id"));
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            return new RazorpayOrderResponse(
                    razorpayOrder.get("id"),
                    razorpayOrder.get("amount"),
                    razorpayOrder.get("currency")
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    @Transactional
    public void verifyRazorpayPayment(RazorpayVerifyRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        boolean updated = paymentRepository.updateStatusIfChanged(
                payment.getId(),
                PaymentStatus.PAYMENT_SUCCESS
        ) == 1;

        if (!updated) {
            log.info("Payment already SUCCESS for orderId={}, skipping publish",
                     payment.getOrderId());
            return;
        }

        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());

        producer.publish(payment);
    }

    @Transactional
    public void markSuccess(Long orderId, String paymentId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow();

        payment.setStatus(PaymentStatus.PAYMENT_SUCCESS);
        payment.setRazorpayPaymentId(paymentId);
        paymentRepository.save(payment);

		/*
		 * PaymentEvent event = new PaymentEvent( orderId,
		 * PaymentStatus.PAYMENT_SUCCESS, payment.getPaymentMethod() );
		 * 
		 * producer.publish(payment);
		 */
    }
    @Transactional
    public void markFailed(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow();

        payment.setStatus(PaymentStatus.PAYMENT_FAILED);
        paymentRepository.save(payment);

		/*
		 * PaymentEvent event = new PaymentEvent( orderId, PaymentStatus.PAYMENT_FAILED,
		 * payment.getPaymentMethod() );
		 * 
		 * producer.publish(payment);
		 */
    }

}