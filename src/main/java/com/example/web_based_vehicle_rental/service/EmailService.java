package com.example.web_based_vehicle_rental.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Objects;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base.url}")
    private String baseUrl;

    /**
     * Send verification email
     */
    public void sendVerificationEmail(@NonNull String to, @NonNull String username, @NonNull String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail == null || fromEmail.isEmpty()) {
                throw new IllegalStateException("Email sender address is not configured");
            }
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail cannot be null"));
            helper.setTo(to);
            helper.setSubject("DriveEase - Verify Your Email");

            // Create context for Thymeleaf template
            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("verificationLink", baseUrl + "/verify-email?token=" + token);

            // Process email template
            String htmlContent = templateEngine.process("email/verification", context);
            helper.setText(Objects.requireNonNull(htmlContent, "htmlContent cannot be null"), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(@NonNull String to, @NonNull String username, @NonNull String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail == null || fromEmail.isEmpty()) {
                throw new IllegalStateException("Email sender address is not configured");
            }
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail cannot be null"));
            helper.setTo(to);
            helper.setSubject("DriveEase - Password Reset Request");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("resetLink", baseUrl + "/reset-password?token=" + token);

            String htmlContent = templateEngine.process("email/password_reset", context);
            helper.setText(Objects.requireNonNull(htmlContent, "htmlContent cannot be null"), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmation(@NonNull String to, @NonNull String username, @NonNull String vehicleName,
            @NonNull String startDate, @NonNull String endDate, @NonNull String totalPrice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail == null || fromEmail.isEmpty()) {
                throw new IllegalStateException("Email sender address is not configured");
            }
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail cannot be null"));
            helper.setTo(to);
            helper.setSubject("DriveEase - Booking Confirmation");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("vehicleName", vehicleName);
            context.setVariable("startDate", startDate);
            context.setVariable("endDate", endDate);
            context.setVariable("totalPrice", totalPrice);

            String htmlContent = templateEngine.process("email/booking_confirmation", context);
            helper.setText(Objects.requireNonNull(htmlContent, "htmlContent cannot be null"), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send booking confirmation email", e);
        }
    }

    /**
     * Send payment receipt email
     */
    public void sendPaymentReceipt(@NonNull String to, @NonNull String username, @NonNull String paymentAmount,
            @NonNull String paymentMethod, @NonNull String transactionId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail == null || fromEmail.isEmpty()) {
                throw new IllegalStateException("Email sender address is not configured");
            }
            helper.setFrom(Objects.requireNonNull(fromEmail, "fromEmail cannot be null"));
            helper.setTo(to);
            helper.setSubject("DriveEase - Payment Receipt");

            Context context = new Context();
            context.setVariable("username", username);
            context.setVariable("paymentAmount", paymentAmount);
            context.setVariable("paymentMethod", paymentMethod);
            context.setVariable("transactionId", transactionId);

            String htmlContent = templateEngine.process("email/payment_receipt", context);
            helper.setText(Objects.requireNonNull(htmlContent, "htmlContent cannot be null"), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send payment receipt email", e);
        }
    }
}
