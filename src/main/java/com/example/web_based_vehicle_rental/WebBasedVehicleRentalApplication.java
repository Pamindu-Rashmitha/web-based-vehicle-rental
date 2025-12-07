package com.example.web_based_vehicle_rental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebBasedVehicleRentalApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebBasedVehicleRentalApplication.class, args);
	}

}
