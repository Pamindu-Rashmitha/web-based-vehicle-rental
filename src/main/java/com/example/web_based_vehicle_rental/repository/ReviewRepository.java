package com.example.web_based_vehicle_rental.repository;

import com.example.web_based_vehicle_rental.model.Review;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a specific vehicle
     */
    List<Review> findByVehicleIdOrderByCreatedDateDesc(Long vehicleId);

    /**
     * Find all reviews by a specific user
     */
    List<Review> findByUserIdOrderByCreatedDateDesc(Long userId);

    /**
     * Find review by user and vehicle (prevent duplicate reviews)
     */
    Optional<Review> findByUserAndVehicle(User user, Vehicle vehicle);

    /**
     * Calculate average rating for a vehicle
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.vehicle.id = :vehicleId")
    Double calculateAverageRating(@Param("vehicleId") Long vehicleId);

    /**
     * Count total reviews for a vehicle
     */
    Long countByVehicleId(Long vehicleId);

    /**
     * Check if user has already reviewed this vehicle
     */
    boolean existsByUserIdAndVehicleId(Long userId, Long vehicleId);

    /**
     * Find top-rated reviews (rating >= 4) for a vehicle
     */
    List<Review> findByVehicleIdAndRatingGreaterThanEqualOrderByCreatedDateDesc(
            Long vehicleId, Integer rating);
}
