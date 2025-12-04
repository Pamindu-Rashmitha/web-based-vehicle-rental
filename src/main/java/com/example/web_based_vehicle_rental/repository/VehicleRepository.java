package com.example.web_based_vehicle_rental.repository;

import com.example.web_based_vehicle_rental.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE' AND v.id NOT IN " +
            "(SELECT r.vehicle.id FROM Reservation r WHERE r.status = 'CONFIRMED' AND " +
            "((r.startDate <= :endDate AND r.endDate >= :startDate)))")
    List<Vehicle> findAvailableVehicles(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE' " +
            "AND (:type IS NULL OR v.type = :type) " +
            "AND (:minPrice IS NULL OR v.dailyPrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.dailyPrice <= :maxPrice) " +
            "AND v.id NOT IN " +
            "(SELECT r.vehicle.id FROM Reservation r WHERE r.status = 'CONFIRMED' AND " +
            "((r.startDate <= :endDate AND r.endDate >= :startDate)))")
    List<Vehicle> findAvailableVehiclesWithFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("type") String type,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice);
}
