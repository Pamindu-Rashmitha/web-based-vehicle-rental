package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // User Management Endpoints

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String role = payload.get("role");
        if (role == null || role.isEmpty()) {
            return ResponseEntity.badRequest().body("Role is required");
        }
        try {
            adminService.updateUserRole(id, role);
            return ResponseEntity.ok("User role updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Vehicle Management Endpoints

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> addVehicle(
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("year") int year,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("dailyPrice") Double dailyPrice,
            @RequestParam("type") String type,
            @RequestParam("status") com.example.web_based_vehicle_rental.model.VehicleStatus status) {
        try {
            String imageUrl = adminService.saveVehicleImage(file);
            Vehicle vehicle = new Vehicle(brand, model, year, licensePlate, dailyPrice, type);
            vehicle.setStatus(status);
            vehicle.setImageUrl(imageUrl);
            return ResponseEntity.ok(adminService.addVehicle(vehicle));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(adminService.getAllVehicles());
    }

    @PutMapping("/vehicles/{id}")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @RequestParam(value = "image", required = false) org.springframework.web.multipart.MultipartFile file,
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("year") int year,
            @RequestParam("licensePlate") String licensePlate,
            @RequestParam("dailyPrice") Double dailyPrice,
            @RequestParam("type") String type,
            @RequestParam("status") com.example.web_based_vehicle_rental.model.VehicleStatus status) {
        try {
            Vehicle vehicle = new Vehicle();
            vehicle.setBrand(brand);
            vehicle.setModel(model);
            vehicle.setYear(year);
            vehicle.setLicensePlate(licensePlate);
            vehicle.setDailyPrice(dailyPrice);
            vehicle.setType(type);
            vehicle.setStatus(status);

            if (file != null && !file.isEmpty()) {
                String imageUrl = adminService.saveVehicleImage(file);
                vehicle.setImageUrl(imageUrl);
            }

            return ResponseEntity.ok(adminService.updateVehicle(id, vehicle));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/vehicles/{id}")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        try {
            adminService.deleteVehicle(id);
            return ResponseEntity.ok("Vehicle deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Cannot delete vehicle. It may have associated reservations.");
        }
    }

    // Reports Endpoints

    @GetMapping("/reports/income")
    public ResponseEntity<List<Object[]>> getMonthlyIncome() {
        return ResponseEntity.ok(adminService.getMonthlyIncome());
    }

    @GetMapping("/reports/popularity")
    public ResponseEntity<List<Object[]>> getMostPopularVehicles() {
        return ResponseEntity.ok(adminService.getMostPopularVehicles());
    }

    @GetMapping("/reports/overdue")
    public ResponseEntity<List<com.example.web_based_vehicle_rental.model.Reservation>> getOverdueReservations() {
        return ResponseEntity.ok(adminService.getOverdueReservations());
    }

    // Reservation Management Endpoints

    @GetMapping("/reservations")
    public ResponseEntity<List<com.example.web_based_vehicle_rental.model.Reservation>> getAllReservations() {
        return ResponseEntity.ok(adminService.getAllReservations());
    }

    // Payment Management Endpoints

    @GetMapping("/payments")
    public ResponseEntity<List<com.example.web_based_vehicle_rental.model.Payment>> getAllPayments() {
        return ResponseEntity.ok(adminService.getAllPayments());
    }

    @GetMapping("/payments/stats")
    public ResponseEntity<?> getPaymentStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRevenue", adminService.getTotalRevenue());
        stats.put("totalRefunds", adminService.getTotalRefunds());
        stats.put("pendingPayments", adminService.getPendingPaymentsCount());
        return ResponseEntity.ok(stats);
    }

    // Vehicle Image Management Endpoints

    @PostMapping("/vehicles/{id}/images")
    public ResponseEntity<?> addVehicleImage(
            @PathVariable Long id,
            @RequestParam("image") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(value = "isPrimary", defaultValue = "false") Boolean isPrimary) {
        try {
            String imageUrl = adminService.saveVehicleImage(file);
            com.example.web_based_vehicle_rental.model.VehicleImage vehicleImage = adminService.addVehicleImage(id,
                    imageUrl, isPrimary);
            return ResponseEntity.ok(vehicleImage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error adding image: " + e.getMessage());
        }
    }

    @DeleteMapping("/vehicles/{vehicleId}/images/{imageId}")
    public ResponseEntity<?> deleteVehicleImage(
            @PathVariable Long vehicleId,
            @PathVariable Long imageId) {
        try {
            adminService.deleteVehicleImage(vehicleId, imageId);
            return ResponseEntity.ok("Image deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting image: " + e.getMessage());
        }
    }

    @PutMapping("/vehicles/{vehicleId}/images/{imageId}/primary")
    public ResponseEntity<?> setPrimaryImage(
            @PathVariable Long vehicleId,
            @PathVariable Long imageId) {
        try {
            adminService.setPrimaryImage(vehicleId, imageId);
            return ResponseEntity.ok("Primary image updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error setting primary image: " + e.getMessage());
        }
    }
}
