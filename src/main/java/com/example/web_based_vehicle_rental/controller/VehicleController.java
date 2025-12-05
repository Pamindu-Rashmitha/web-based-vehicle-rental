package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    private final VehicleRepository vehicleRepository;

    public VehicleController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("") // → /api/vehicles
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        // Force initialization of images while session is open
        vehicles.forEach(v -> v.getImages().size()); // ← THIS LINE FIXES EVERYTHING
        return vehicles;
    }

    @GetMapping("/{id}") // → /api/vehicles/5
    @Transactional(readOnly = true)
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable @NonNull Long id) {
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicle.getImages().size(); // initialize images
                    return ResponseEntity.ok(vehicle);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}