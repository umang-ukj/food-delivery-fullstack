package com.fd.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendWelcomeEmail(String toEmail) {
        sendEmail(
                toEmail,
                "Welcome to Food Delivery",
                "Welcome to Food Delivery app!\n\nWe're glad to have you with us."
        );
    }

    public void sendPasswordResetEmail(String toEmail, String newPassword) {
        sendEmail(
                toEmail,
                "Food Delivery - New Temporary Password",
                "Your password has been reset.\n\nTemporary password: " + newPassword +
                        "\n\nPlease login and change your password immediately."
        );
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