package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import com.example.web_based_vehicle_rental.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Email verification endpoint
     */
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        boolean isVerified = verificationService.verifyEmail(token);

        if (isVerified) {
            model.addAttribute("success", true);
            model.addAttribute("message", "Your email has been successfully verified! You can now log in.");
        } else {
            model.addAttribute("success", false);
            model.addAttribute("message",
                    "Invalid or expired verification link. Please request a new verification email.");
        }

        return "verification_result";
    }

    /**
     * Resend verification email
     */
    @PostMapping("/resend-verification")
    public String resendVerification(@RequestParam("email") String email, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "No account found with that email address.");
            return "resend_verification";
        }

        User user = userOpt.get();

        if (user.isEmailVerified()) {
            model.addAttribute("error", "Your email is already verified. You can log in.");
            return "resend_verification";
        }

        verificationService.resendVerificationEmail(user);
        model.addAttribute("success", "Verification email sent! Please check your inbox.");

        return "resend_verification";
    }

    /**
     * Show forgot password page
     */
    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot_password";
    }

    /**
     * Process forgot password request
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            // Don't reveal if email exists for security
            model.addAttribute("success", "If an account exists with that email, a password reset link has been sent.");
            return "forgot_password";
        }

        User user = userOpt.get();
        verificationService.createPasswordResetToken(user);

        model.addAttribute("success", "Password reset link has been sent to your email.");
        return "forgot_password";
    }

    /**
     * Show reset password page
     */
    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam("token") String token, Model model) {
        Optional<User> userOpt = verificationService.validatePasswordResetToken(token);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid or expired password reset link.");
            return "reset_password";
        }

        model.addAttribute("token", token);
        return "reset_password";
    }

    /**
     * Process password reset
     */
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
        // Validate passwords match
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset_password";
        }

        // Validate password strength
        if (password.length() < 8) {
            model.addAttribute("error", "Password must be at least 8 characters long.");
            model.addAttribute("token", token);
            return "reset_password";
        }

        // Get user from token
        Optional<User> userOpt = verificationService.validatePasswordResetToken(token);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid or expired password reset link.");
            return "reset_password";
        }

        // Reset password
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        // Mark token as used
        verificationService.resetPassword(token, password);

        model.addAttribute("success", "Your password has been reset successfully! You can now log in.");
        return "reset_password";
    }
}
