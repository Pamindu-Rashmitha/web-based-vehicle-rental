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
import java.util.Objects;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final VehicleRepository vehicleRepository;
    private final StripeService stripeService;

    public ReservationService(ReservationRepository reservationRepository, VehicleRepository vehicleRepository,
            StripeService stripeService) {
        this.reservationRepository = reservationRepository;
        this.vehicleRepository = vehicleRepository;
        this.stripeService = stripeService;
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

    @Transactional
    public void cancelReservation(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Reservation ID cannot be null");
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel past or active reservations");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Transactional
    public void extendReservation(Long reservationId, int extraDays) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Reservation ID cannot be null");
        }
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed reservations can be extended");
        }

        LocalDate currentEndDate = reservation.getEndDate();
        LocalDate newEndDate = currentEndDate.plusDays(extraDays);

        // Check availability for the extension period
        LocalDate extensionStartDate = currentEndDate.plusDays(1);

        List<Vehicle> availableVehicles = vehicleRepository.findAvailableVehicles(extensionStartDate, newEndDate);
        boolean isAvailable = availableVehicles.stream()
                .anyMatch(v -> Objects.equals(v.getId(), reservation.getVehicle().getId()));

        if (!isAvailable) {
            throw new IllegalStateException("Vehicle is not available for the requested extension period");
        }

        reservation.setEndDate(newEndDate);

        // Recalculate price
        long totalDays = ChronoUnit.DAYS.between(reservation.getStartDate(), newEndDate) + 1;
        reservation.setTotalPrice(reservation.getVehicle().getDailyPrice() * totalDays);

        reservationRepository.save(reservation);
    }

    // Create Stripe checkout session for a reservation
    @Transactional
    public String createPaymentCheckout(Reservation reservation) {
        try {
            String baseUrl = "http://localhost:8080";
            String successUrl = baseUrl + "/payment/success";
            String cancelUrl = baseUrl + "/payment/cancel";

            return stripeService.createCheckoutSession(reservation, successUrl, cancelUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment checkout: " + e.getMessage(), e);
        }
    }

    // Cancel reservation with refund if payment exists

    @Transactional
    public String cancelReservationWithRefund(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Reservation ID cannot be null");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        // Check if there's a payment to refund
        if (reservation.getPayment() != null &&
                reservation.getPayment()
                        .getStatus() == com.example.web_based_vehicle_rental.model.PaymentStatus.SUCCEEDED) {
            try {
                // Process refund with cancellation fee
                com.example.web_based_vehicle_rental.model.Payment refundedPayment = stripeService
                        .processRefund(reservation.getPayment());

                reservation.setPayment(refundedPayment);
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationRepository.save(reservation);

                double refundAmount = refundedPayment.getRefundAmount();
                double fee = refundedPayment.getAmount() - refundAmount;

                return String.format(
                        "Reservation cancelled successfully. Refund of $%.2f processed (cancellation fee: $%.2f)",
                        refundAmount, fee);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process refund: " + e.getMessage(), e);
            }
        } else {
            // No payment or payment not successful, just cancel
            reservation.setStatus(ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            return "Reservation cancelled successfully";
        }
    }
}
