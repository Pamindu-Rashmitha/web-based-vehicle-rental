package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReservationViewController {

    private final UserService userService;

    public ReservationViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/dashboard")
    public String showUserDashboard(Model model) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            java.util.List<com.example.web_based_vehicle_rental.model.Reservation> allReservations = userService
                    .getUserReservations(username);

            java.time.LocalDate today = java.time.LocalDate.now();

            java.util.List<com.example.web_based_vehicle_rental.model.Reservation> upcoming = new java.util.ArrayList<>();
            java.util.List<com.example.web_based_vehicle_rental.model.Reservation> active = new java.util.ArrayList<>();
            java.util.List<com.example.web_based_vehicle_rental.model.Reservation> past = new java.util.ArrayList<>();

            for (com.example.web_based_vehicle_rental.model.Reservation r : allReservations) {
                if (r.getStatus() == com.example.web_based_vehicle_rental.model.ReservationStatus.CANCELLED) {
                    past.add(r);
                } else if (r.getEndDate() != null && r.getEndDate().isBefore(today)) {
                    past.add(r);
                } else if (r.getStartDate() != null && r.getStartDate().isAfter(today)) {
                    upcoming.add(r);
                } else {
                    if (r.getStartDate() != null && r.getEndDate() != null) {
                        active.add(r);
                    } else {
                        past.add(r);
                    }
                }
            }

            model.addAttribute("upcomingReservations", upcoming);
            model.addAttribute("activeReservations", active);
            model.addAttribute("pastReservations", past);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while loading rentals: " + e.getMessage());
            model.addAttribute("upcomingReservations", new java.util.ArrayList<>());
            model.addAttribute("activeReservations", new java.util.ArrayList<>());
            model.addAttribute("pastReservations", new java.util.ArrayList<>());
        }

        return "user_dashboard";
    }
}
