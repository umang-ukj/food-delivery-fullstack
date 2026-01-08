package com.fd.payment.service;

import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fd.payment.config.RazorpayConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Service
public class RazorpayClientService {

    private static final Logger log = LoggerFactory.getLogger(RazorpayClientService.class);
    
    @Value("${razorpay.key.secret}")
    private String razorpaySecret;
    
    private final RazorpayConfig razorpayConfig;

    public RazorpayClientService(RazorpayConfig razorpayConfig) {
        this.razorpayConfig = razorpayConfig;
    }

    @CircuitBreaker(name = "razorpay", fallbackMethod = "createOrderFallback")
    @Retry(name = "razorpay")
    public Order createOrder(JSONObject options) throws Exception {

        log.info("Calling Razorpay create order");
        return razorpayConfig.razorpayClient().orders.create(options);
    }

    public Order createOrderFallback(
            JSONObject options,
            Throwable ex) {

        log.error("Razorpay CREATE ORDER failed", ex);
        throw new RuntimeException("Razorpay unavailable", ex);
    }
    @CircuitBreaker(name = "razorpay", fallbackMethod = "verifyFallback")
    public void verifySignature(Map<String, String> payload)
            throws RazorpayException {

        log.info("Verifying Razorpay signature");
        JSONObject jsonPayload=new JSONObject(payload);
        Utils.verifyPaymentSignature(jsonPayload, razorpaySecret);
    }

    public void verifyFallback(
            Map<String, String> payload,
            Throwable ex) {

        log.error("Razorpay SIGNATURE verification failed", ex);
        throw new RuntimeException("RAZORPAY_SIGNATURE_VERIFICATION_FAILED", ex);
    }
}
