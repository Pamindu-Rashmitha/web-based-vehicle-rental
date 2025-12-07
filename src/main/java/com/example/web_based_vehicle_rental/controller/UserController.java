package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String showLandingPage() {
        return "landing";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            Model model) {
        // Check for validation errors
        if (result.hasErrors()) {
            return "register";
        }

        // Check terms agreement
        if (!user.isAgreedToTerms()) {
            model.addAttribute("error", "You must agree to the Privacy Policy and terms of use.");
            return "register";
        }

        try {
            userService.register(user);
            model.addAttribute("email", user.getEmail());
            return "registration_success";
        } catch (IllegalStateException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/privacy")
    public String showPrivacyPolicy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String showTermsOfService() {
        return "terms";
    }

    @GetMapping("/about")
    public String showAboutUs() {
        return "about";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User updatedUser, Model model) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            String newUsername = updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()
                    ? updatedUser.getUsername()
                    : currentUsername;

            userService.updateProfile(currentUsername, updatedUser);

            if (!newUsername.equals(currentUsername)) {
                User updatedUserFromDb = userService.findByUsername(newUsername)
                        .orElseThrow(() -> new IllegalArgumentException("Updated user not found"));
                Authentication newAuth = new UsernamePasswordAuthenticationToken(
                        updatedUserFromDb,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + updatedUserFromDb.getRole())));
                SecurityContextHolder.getContext().setAuthentication(newAuth);
            }

            return "redirect:/profile?success=Profile updated successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", updatedUser);
            return "profile";
        }
    }

    @PostMapping("/delete-account")
    public String deleteAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.deleteAccount(username);
        return "redirect:/login";
    }
}