package com.example.web_based_vehicle_rental.config;

import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if admin exists, if not, create one
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Change this password!
                admin.setEmail("admin@driveease.com");
                admin.setRole("ADMIN");
                admin.setAgreedToTerms(true);
                userRepository.save(admin);
                System.out.println("Default admin created: username=admin");
            }
        };
    }
}