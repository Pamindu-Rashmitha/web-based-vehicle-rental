package com.example.web_based_vehicle_rental.repository;

import com.example.web_based_vehicle_rental.model.SupportRequest;
import com.example.web_based_vehicle_rental.model.SupportStatus;
import com.example.web_based_vehicle_rental.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    // Find all requests by user, ordered by creation date (newest first)
    List<SupportRequest> findByUserOrderByCreatedAtDesc(User user);

    // Find all requests by status, ordered by creation date
    List<SupportRequest> findByStatusOrderByCreatedAtDesc(SupportStatus status);

    // Find all requests ordered by creation date (for admin)
    List<SupportRequest> findAllByOrderByCreatedAtDesc();

    // Count open requests for a user
    long countByUserAndStatus(User user, SupportStatus status);

    // Count all open requests (for admin dashboard)
    long countByStatus(SupportStatus status);
}
