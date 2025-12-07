package com.example.web_based_vehicle_rental.scheduler;

import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.ReservationStatus;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler service that automatically completes expired vehicle rentals.
 * 
 * This service runs daily at midnight to find all confirmed reservations
 * that have passed their end date and marks them as COMPLETED, freeing
 * the vehicles for new bookings.
 */
@Component
public class RentalCompletionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RentalCompletionScheduler.class);

    private final ReservationRepository reservationRepository;

    public RentalCompletionScheduler(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Scheduled task that runs daily at midnight to complete expired rentals.
     * 
     * Finds all CONFIRMED reservations where endDate < today and updates them to
     * COMPLETED.
     * This ensures vehicles become available again and reservation status stays
     * accurate.
     */
    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    public void completeExpiredRentals() {
        LocalDate today = LocalDate.now();

        logger.info("Starting automatic rental completion check for date: {}", today);

        try {
            // Find all CONFIRMED reservations that have ended
            List<Reservation> expiredReservations = reservationRepository
                    .findExpiredReservationsByStatus(today, ReservationStatus.CONFIRMED);

            if (expiredReservations.isEmpty()) {
                logger.info("No expired rentals found to complete");
                return;
            }

            logger.info("Found {} expired rental(s) to complete", expiredReservations.size());

            int completedCount = 0;
            for (Reservation reservation : expiredReservations) {
                try {
                    // Update status to COMPLETED
                    reservation.setStatus(ReservationStatus.COMPLETED);
                    reservationRepository.save(reservation);

                    completedCount++;
                    logger.info("Completed rental ID: {} for user: {} (Vehicle: {} {} - ended on {})",
                            reservation.getId(),
                            reservation.getUser().getUsername(),
                            reservation.getVehicle().getBrand(),
                            reservation.getVehicle().getModel(),
                            reservation.getEndDate());

                } catch (Exception e) {
                    logger.error("Error completing rental ID: {}", reservation.getId(), e);
                }
            }

            logger.info("Successfully completed {} out of {} expired rentals",
                    completedCount, expiredReservations.size());

        } catch (Exception e) {
            logger.error("Error during automatic rental completion process", e);
        }
    }

    /**
     * Manual trigger for rental completion (for testing or admin use).
     * Can be called via an admin API endpoint if needed.
     */
    public int manuallyCompleteExpiredRentals() {
        LocalDate today = LocalDate.now();
        List<Reservation> expiredReservations = reservationRepository
                .findExpiredReservationsByStatus(today, ReservationStatus.CONFIRMED);

        int count = 0;
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(ReservationStatus.COMPLETED);
            reservationRepository.save(reservation);
            count++;
        }

        logger.info("Manually completed {} expired rentals", count);
        return count;
    }
}
