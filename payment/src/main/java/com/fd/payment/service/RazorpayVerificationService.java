package com.fd.payment.service;

import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayVerificationService {

    @Value("${razorpay.key.secret}")
    private String secret;

    public boolean verify(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes());
            String generatedSignature =bytesToHex(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }
    
    //razorpay uses HEX
    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }
        return hex.toString();
    }
}
