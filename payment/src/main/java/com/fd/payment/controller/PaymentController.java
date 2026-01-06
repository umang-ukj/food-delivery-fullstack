package com.fd.payment.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fd.payment.dto.PaymentFailureRequest;
import com.fd.payment.dto.PaymentRequest;
import com.fd.payment.dto.PaymentResponse;
import com.fd.payment.dto.RazorpayOrderRequest;
import com.fd.payment.dto.RazorpayOrderResponse;
import com.fd.payment.dto.RazorpayVerifyRequest;
import com.fd.payment.service.PaymentService;
import com.fd.payment.service.RazorpayVerificationService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayVerificationService verificationService;
    public PaymentController(PaymentService paymentService, RazorpayVerificationService verificationService) {
        this.paymentService = paymentService;
        this.verificationService=verificationService;
    }

    @PostMapping
    public PaymentResponse createPayment(
            @RequestBody PaymentRequest request) {

        return paymentService.createPayment(request);
    }

    @GetMapping("/order/{orderId}")
    public PaymentResponse getPaymentByOrder(@PathVariable Long orderId) {

        return paymentService.getPaymentByOrder(orderId);
    }
    
    @PostMapping("/razorpay/order")
    public RazorpayOrderResponse createRazorpayOrder(
            @RequestBody RazorpayOrderRequest request) {

        return paymentService.createRazorpayOrder(
                request.getOrderId(),
                request.getAmount()
        );
    }
    @PostMapping("/razorpay/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(
            @RequestBody RazorpayVerifyRequest request
    ) {
        boolean isValid = verificationService.verify(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!isValid) {
            paymentService.markFailed(request.getOrderId());
            return ResponseEntity.ok(
                    Map.of("status", "SUCCESS")
            	    );
        }

        paymentService.markSuccess(
                request.getOrderId(),
                request.getRazorpayPaymentId()
        );

        return ResponseEntity.ok().build();
    }
    @PostMapping("/razorpay/failure")
    public ResponseEntity<Void> paymentFailed(
            @RequestBody PaymentFailureRequest request) {

        paymentService.markPaymentFailed(
            request.getOrderId(),
            request.getRazorpayOrderId()
        );

        return ResponseEntity.ok().build();
    }


}
