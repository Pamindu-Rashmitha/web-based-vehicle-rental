package com.example.web_based_vehicle_rental.repository;

import com.example.web_based_vehicle_rental.model.VehicleImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleImageRepository extends JpaRepository<VehicleImage, Long> {

    List<VehicleImage> findByVehicleId(Long vehicleId);

    Optional<VehicleImage> findByVehicleIdAndIsPrimary(Long vehicleId, Boolean isPrimary);

    void deleteByVehicleId(Long vehicleId);
}
