package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class PublicVehicleViewController {

    @GetMapping("/vehicles")
    public String vehiclesPage() {
        return "vehicles";
    }
}

@RestController
class PublicVehicleApiController {

    private final VehicleRepository vehicleRepository;

    public PublicVehicleApiController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/api/public/vehicles")
    @Transactional(readOnly = true)
    public List<Vehicle> getAllVehiclesPublic() {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        // Force initialization of images while session is open
        vehicles.forEach(v -> v.getImages().size());
        return vehicles;
    }

    @GetMapping("/api/public/vehicles/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Vehicle> getVehicleByIdPublic(@PathVariable @NonNull Long id) {
        return vehicleRepository.findById(id)
                .map(vehicle -> {
                    vehicle.getImages().size(); // initialize images
                    return ResponseEntity.ok(vehicle);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
