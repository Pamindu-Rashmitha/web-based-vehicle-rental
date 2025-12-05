package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.repository.UserRepository;

import com.example.web_based_vehicle_rental.repository.VehicleRepository;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import com.example.web_based_vehicle_rental.repository.PaymentRepository;
import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.ReservationStatus;
import java.time.LocalDate;
import java.util.Arrays;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;
    private final com.example.web_based_vehicle_rental.repository.VehicleImageRepository vehicleImageRepository;

    public AdminService(UserRepository userRepository, VehicleRepository vehicleRepository,
            ReservationRepository reservationRepository,
            PaymentRepository paymentRepository,
            com.example.web_based_vehicle_rental.repository.VehicleImageRepository vehicleImageRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
        this.vehicleImageRepository = vehicleImageRepository;
    }

    // User Management
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserRole(Long userId, String role) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (userId == null)
            throw new IllegalArgumentException("User ID cannot be null");
        userRepository.deleteById(userId);
    }

    // Vehicle Management
    public Vehicle addVehicle(Vehicle vehicle) {
        if (vehicle == null)
            throw new IllegalArgumentException("Vehicle cannot be null");
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        if (id == null)
            throw new IllegalArgumentException("Vehicle ID cannot be null");
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        vehicle.setBrand(vehicleDetails.getBrand());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setYear(vehicleDetails.getYear());
        vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        vehicle.setStatus(vehicleDetails.getStatus());
        vehicle.setType(vehicleDetails.getType());
        vehicle.setDailyPrice(vehicleDetails.getDailyPrice());
        if (vehicleDetails.getImageUrl() != null) {
            vehicle.setImageUrl(vehicleDetails.getImageUrl());
        }

        return vehicleRepository.save(vehicle);
    }

    public String saveVehicleImage(org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        String uploadDir = "C:/Users/pamid/vehicle-rental-images/vehicles/";
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
        if (!java.nio.file.Files.exists(uploadPath)) {
            java.nio.file.Files.createDirectories(uploadPath);
        }
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        java.nio.file.Path filePath = uploadPath.resolve(fileName);
        java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return "/images/vehicles/" + fileName;
    }

    public void deleteVehicle(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Vehicle ID cannot be null");
        if (!vehicleRepository.existsById(id)) {
            throw new IllegalArgumentException("Vehicle not found with ID: " + id);
        }
        vehicleRepository.deleteById(id);
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Optional<Vehicle> getVehicleById(Long id) {
        if (id == null)
            throw new IllegalArgumentException("Vehicle ID cannot be null");
        return vehicleRepository.findById(id);
    }

    // Reports
    public List<Object[]> getMonthlyIncome() {
        return reservationRepository.findIncomeByMonth();
    }

    public List<Object[]> getMostPopularVehicles() {
        return reservationRepository.findMostPopularVehicles();
    }

    public List<Reservation> getOverdueReservations() {
        return reservationRepository.findOverdueReservations(
                LocalDate.now(),
                Arrays.asList(ReservationStatus.COMPLETED, ReservationStatus.CANCELLED));
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Payment Management
    public List<com.example.web_based_vehicle_rental.model.Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Double getTotalRevenue() {
        Double revenue = paymentRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    public Double getTotalRefunds() {
        Double refunds = paymentRepository.getTotalRefunds();
        return refunds != null ? refunds : 0.0;
    }

    public Long getPendingPaymentsCount() {
        Long count = paymentRepository.countPendingPayments();
        return count != null ? count : 0L;
    }

    // Vehicle Image Management
    public com.example.web_based_vehicle_rental.model.VehicleImage addVehicleImage(Long vehicleId, String imageUrl,
            Boolean isPrimary) {
        if (vehicleId == null)
            throw new IllegalArgumentException("Vehicle ID cannot be null");

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // If this is set as primary, unset all other primary images for this vehicle
        if (isPrimary != null && isPrimary) {
            vehicle.getImages().forEach(img -> img.setIsPrimary(false));
        }

        com.example.web_based_vehicle_rental.model.VehicleImage image = new com.example.web_based_vehicle_rental.model.VehicleImage(
                vehicle, imageUrl, isPrimary);
        vehicle.addImage(image);

        // Update vehicle's imageUrl field if this is the primary image
        if (isPrimary != null && isPrimary) {
            vehicle.setImageUrl(imageUrl);
        }

        vehicleRepository.save(vehicle);
        return image;
    }

    public void deleteVehicleImage(Long vehicleId, Long imageId) {
        if (vehicleId == null)
            throw new IllegalArgumentException("Vehicle ID cannot be null");
        if (imageId == null)
            throw new IllegalArgumentException("Image ID cannot be null");

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        com.example.web_based_vehicle_rental.model.VehicleImage imageToRemove = vehicle.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Image not found"));

        vehicle.removeImage(imageToRemove);
        vehicleImageRepository.delete(Objects.requireNonNull(imageToRemove));
        vehicleRepository.save(vehicle);
    }

    public void setPrimaryImage(Long vehicleId, Long imageId) {
        if (vehicleId == null)
            throw new IllegalArgumentException("Vehicle ID cannot be null");
        if (imageId == null)
            throw new IllegalArgumentException("Image ID cannot be null");

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        // Unset all primary images
        vehicle.getImages().forEach(img -> img.setIsPrimary(false));

        // Set the specified image as primary
        com.example.web_based_vehicle_rental.model.VehicleImage primaryImage = vehicle.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Image not found"));

        primaryImage.setIsPrimary(true);
        vehicle.setImageUrl(primaryImage.getImageUrl()); // Update backward compatibility field

        vehicleRepository.save(vehicle);
    }
}
