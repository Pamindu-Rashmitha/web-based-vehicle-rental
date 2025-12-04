package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.service.ReservationService;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    public ReservationController(ReservationService reservationService, UserRepository userRepository) {
        this.reservationService = reservationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<Vehicle>> searchVehicles(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice) {
        return ResponseEntity.ok(
                reservationService.searchAvailableVehiclesWithFilters(startDate, endDate, type, minPrice, maxPrice));
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookVehicle(@RequestBody Map<String, Object> bookingRequest) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Long vehicleId = Long.valueOf(bookingRequest.get("vehicleId").toString());
            LocalDate startDate = LocalDate.parse(bookingRequest.get("startDate").toString());
            LocalDate endDate = LocalDate.parse(bookingRequest.get("endDate").toString());

            Reservation reservation = reservationService.createReservation(user, vehicleId, startDate, endDate);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancelReservation(id);
            return ResponseEntity.ok("Reservation cancelled successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<?> extendReservation(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        try {
            Integer extraDays = payload.get("extraDays");
            if (extraDays == null || extraDays <= 0) {
                return ResponseEntity.badRequest().body("Invalid extra days");
            }
            reservationService.extendReservation(id, extraDays);
            return ResponseEntity.ok("Reservation extended successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
