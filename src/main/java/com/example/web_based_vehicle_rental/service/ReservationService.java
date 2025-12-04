package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.ReservationStatus;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import com.example.web_based_vehicle_rental.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;

    public ReservationService(ReservationRepository reservationRepository, VehicleRepository vehicleRepository) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> searchAvailableVehicles(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        return vehicleRepository.findAvailableVehicles(startDate, endDate);
    }

    public List<Vehicle> searchAvailableVehiclesWithFilters(LocalDate startDate, LocalDate endDate, String type,
            Double minPrice, Double maxPrice) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        // Handle empty strings for type if necessary, though controller usually handles
        // nulls
        if (type != null && type.trim().isEmpty()) {
            type = null;
        }
        return vehicleRepository.findAvailableVehiclesWithFilters(startDate, endDate, type, minPrice, maxPrice);
    }

    @Transactional
    public Reservation createReservation(User user, Long vehicleId, LocalDate startDate, LocalDate endDate) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("Vehicle ID cannot be null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        // Double-check availability to prevent race conditions (simple check)
        List<Vehicle> availableVehicles = vehicleRepository.findAvailableVehicles(startDate, endDate);
        boolean isAvailable = availableVehicles.stream().anyMatch(v -> v.getId().equals(vehicleId));

        if (!isAvailable) {
            throw new IllegalStateException("Vehicle is not available for the selected dates");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        Double totalPrice = vehicle.getDailyPrice() * days;

        Reservation reservation = new Reservation(user, vehicle, startDate, endDate, totalPrice);
        reservation.setStatus(ReservationStatus.CONFIRMED); // Auto-confirm for now

        return reservationRepository.save(reservation);
    }
}
