package com.example.web_based_vehicle_rental.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:C:/Users/pamid/vehicle-rental-images/");

        // Serve uploaded support screenshots
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
