package com.fd.order.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fd.order.entity.Order;
import com.fd.order.entity.OrderItem;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Async
    public void sendOrderConfirmationEmail(String toEmail, Order order) {
        String subject = "Food Delivery - Order Confirmation from " + order.getRestaurantName();
        String body = "Your order has been placed successfully.\n\n"
                + "Order ID: " + order.getId() + "\n"
                //+ "Restaurant ID: " + order.getRestaurantId() + "\n"
                //+ "Total Amount: " + order.getTotalAmount() + "\n"
                + "Restaurant: " + valueOrNA(order.getRestaurantName()) + "\n"
                + "Restaurant Image: " + valueOrNA(order.getRestaurantImageUrl()) + "\n"
                + buildPriceBreakdown(order)
                + "Ordered Time: " + formatTime(order.getOrderedAt()) + "\n"
                + "Current Status: " + order.getStatus();

        sendEmail(toEmail, subject, body);
    }
    @Async
    public void sendOrderDeliveredEmail(String toEmail, Order order) {
        String subject = "Food Delivery - Order Delivered from " +order.getRestaurantName();
        String body = "Your order has been delivered successfully.\n\n"
                + "Order ID: " + order.getId() + "\n"
                //+ "Total Amount: " + order.getTotalAmount() + "\n"
                + "Restaurant: " + valueOrNA(order.getRestaurantName()) + "\n"
                + "Restaurant Image: " + valueOrNA(order.getRestaurantImageUrl()) + "\n"
                + buildPriceBreakdown(order)
                + "Ordered Time: " + formatTime(order.getOrderedAt()) + "\n"
                + "Delivered Time: " + formatTime(order.getDeliveredAt()) + "\n"
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
    
    private String buildPriceBreakdown(Order order) {
        StringBuilder breakdown = new StringBuilder("Items:\n");
        double computedTotal = 0;

        for (OrderItem item : order.getItems()) {
            double lineTotal = item.getPrice() * item.getQuantity();
            computedTotal += lineTotal;
            breakdown.append("- ")
                    .append(item.getName())
                    .append(" (x")
                    .append(item.getQuantity())
                    .append("): ")
                    .append(formatCurrency(lineTotal))
                    .append(" [")
                    .append(formatCurrency(item.getPrice()))
                    .append(" each]\n");
        }

        breakdown.append("Subtotal: ").append(formatCurrency(computedTotal)).append("\n");
        breakdown.append("Total Amount: ").append(formatCurrency(order.getTotalAmount())).append("\n\n");
        return breakdown.toString();
    }

    private String valueOrNA(String value) {
        return value == null || value.isBlank() ? "N/A" : value;
    }

    private String formatCurrency(double amount) {
        return "₹" + String.format(Locale.US, "%.2f", amount);
    }
    private String formatTime(LocalDateTime time) {
        return time == null ? "N/A" : time.format(TIME_FORMATTER);
    }
}
