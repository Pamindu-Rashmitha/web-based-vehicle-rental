package com.example.web_based_vehicle_rental.config;

import com.example.web_based_vehicle_rental.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        private final UserRepository userRepository;
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

        public SecurityConfig(UserRepository userRepository,
                        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) {
                this.userRepository = userRepository;
                this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return username -> userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/", "/register", "/login", "/privacy", "/css/**",
                                                                "/js/**", "/images/**",
                                                                "/browse", "/api/reservations/search")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/reservations/**")); // Optional:
                                                                                                     // Disable CSRF for
                                                                                                     // API
                                                                                                     // if needed, but
                                                                                                     // better to use
                                                                                                     // token
                return http.build();
        }
}