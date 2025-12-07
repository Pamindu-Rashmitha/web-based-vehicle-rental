package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.PasswordResetToken;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.VerificationToken;
import com.example.web_based_vehicle_rental.repository.PasswordResetTokenRepository;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import com.example.web_based_vehicle_rental.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Create and send verification token
     */
    @Transactional
    public void createVerificationToken(User user) {
        // Delete any existing tokens for this user
        verificationTokenRepository.findByUser(user).ifPresent(verificationTokenRepository::delete);

        // Generate token
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);

        // Send verification email
        emailService.sendVerificationEmail(
                Objects.requireNonNull(user.getEmail(), "user email cannot be null"),
                Objects.requireNonNull(user.getUsername(), "username cannot be null"),
                Objects.requireNonNull(token, "token cannot be null"));
    }

    /**
     * Verify email with token
     */
    @Transactional
    public boolean verifyEmail(String token) {
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        VerificationToken verificationToken = tokenOpt.get();

        // Check if token is expired
        if (verificationToken.isExpired()) {
            return false;
        }

        // Verify user
        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Delete token after successful verification
        verificationTokenRepository.delete(verificationToken);

        return true;
    }

    /**
     * Create password reset token
     */
    @Transactional
    public void createPasswordResetToken(User user) {
        // Delete any existing reset tokens
        passwordResetTokenRepository.findByUser(user).ifPresent(passwordResetTokenRepository::delete);

        // Generate token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(resetToken);

        // Send reset email
        emailService.sendPasswordResetEmail(
                Objects.requireNonNull(user.getEmail(), "user email cannot be null"),
                Objects.requireNonNull(user.getUsername(), "username cannot be null"),
                Objects.requireNonNull(token, "token cannot be null"));
    }

    /**
     * Validate password reset token
     */
    public Optional<User> validatePasswordResetToken(String token) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return Optional.empty();
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Check if token is valid
        if (!resetToken.isValid()) {
            return Optional.empty();
        }

        return Optional.of(resetToken.getUser());
    }

    /**
     * Reset password using token
     */
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (!resetToken.isValid()) {
            return false;
        }

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        return true;
    }

    /**
     * Resend verification email
     */
    @Transactional
    public void resendVerificationEmail(User user) {
        createVerificationToken(user);
    }
}
