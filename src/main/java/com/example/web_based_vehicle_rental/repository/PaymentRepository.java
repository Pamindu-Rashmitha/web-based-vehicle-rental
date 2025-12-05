package com.example.web_based_vehicle_rental.repository;

import com.example.web_based_vehicle_rental.model.Payment;
import com.example.web_based_vehicle_rental.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByStripeSessionId(String stripeSessionId);

    Optional<Payment> findByReservationId(Long reservationId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.status = 'SUCCEEDED'")
    Double getTotalRevenue();

    @Query("SELECT SUM(p.refundAmount) FROM Payment p WHERE p.status IN ('REFUNDED', 'PARTIALLY_REFUNDED')")
    Double getTotalRefunds();

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'PENDING'")
    Long countPendingPayments();
}
