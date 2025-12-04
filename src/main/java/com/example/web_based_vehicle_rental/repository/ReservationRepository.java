package com.example.web_based_vehicle_rental.repository;

import com.example.web_based_vehicle_rental.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT YEAR(r.startDate), MONTH(r.startDate), SUM(r.totalPrice) FROM Reservation r WHERE r.status = com.example.web_based_vehicle_rental.model.ReservationStatus.CONFIRMED OR r.status = com.example.web_based_vehicle_rental.model.ReservationStatus.COMPLETED GROUP BY YEAR(r.startDate), MONTH(r.startDate) ORDER BY YEAR(r.startDate) DESC, MONTH(r.startDate) DESC")
    List<Object[]> findIncomeByMonth();

    @org.springframework.data.jpa.repository.Query("SELECT r.vehicle, COUNT(r) as rentalCount FROM Reservation r GROUP BY r.vehicle ORDER BY rentalCount DESC")
    List<Object[]> findMostPopularVehicles();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Reservation r WHERE r.endDate < :today AND r.status NOT IN (:statuses)")
    List<Reservation> findOverdueReservations(
            @org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
            @org.springframework.data.repository.query.Param("statuses") List<com.example.web_based_vehicle_rental.model.ReservationStatus> statuses);
}
