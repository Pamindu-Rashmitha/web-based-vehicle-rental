package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Vehicle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Controller
public class BrowseViewController {

    private final com.example.web_based_vehicle_rental.repository.VehicleRepository vehicleRepository;

    public BrowseViewController(com.example.web_based_vehicle_rental.repository.VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/browse")
    public String browse() {
        return "browse";
    }

    @GetMapping("/booking/confirm")
    public String confirmBooking(@RequestParam Long vehicleId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Model model) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        double totalPrice = vehicle.getDailyPrice() * days;

        model.addAttribute("vehicle", vehicle);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("days", days);

        return "booking_confirmation";
    }
}
