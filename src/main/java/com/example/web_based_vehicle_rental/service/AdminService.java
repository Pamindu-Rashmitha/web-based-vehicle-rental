package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import com.example.web_based_vehicle_rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public AdminService(UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
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
}
