package com.fd.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.fd.order.entity.Order;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmationEmail(String toEmail, Order order) {
        String subject = "Food Delivery - Order Confirmation #" + order.getId();
        String body = "Your order has been placed successfully.\n\n"
                + "Order ID: " + order.getId() + "\n"
                + "Restaurant ID: " + order.getRestaurantId() + "\n"
                + "Total Amount: " + order.getTotalAmount() + "\n"
                + "Current Status: " + order.getStatus();

        sendEmail(toEmail, subject, body);
    }
    
    public void sendOrderDeliveredEmail(String toEmail, Order order) {
        String subject = "Food Delivery - Order Delivered #" + order.getId();
        String body = "Your order has been delivered successfully.\n\n"
                + "Order ID: " + order.getId() + "\n"
                + "Total Amount: " + order.getTotalAmount() + "\n"
                + "Status: " + order.getStatus() + "\n\n"
                + "Thank you for ordering with Food Delivery!";

        sendEmail(toEmail, subject, body);
    }
    
    private void sendEmail(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", toEmail, ex);
        }
    }
}
