package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.Review;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.model.Vehicle;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import com.example.web_based_vehicle_rental.repository.ReviewRepository;
import com.example.web_based_vehicle_rental.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    /**
     * Create a new review
     */
    @Transactional
    public Review createReview(User user, Long vehicleId, Integer rating, String comment, Long reservationId) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(vehicleId, "Vehicle ID cannot be null");
        Objects.requireNonNull(rating, "Rating cannot be null");

        // Validate rating
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Check if vehicle exists
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        // Check if user has already reviewed this vehicle
        if (reviewRepository.existsByUserIdAndVehicleId(user.getId(), vehicleId)) {
            throw new IllegalStateException("You have already reviewed this vehicle");
        }

        // Optional: Verify user has completed a reservation for this vehicle
        boolean hasCompletedReservation = reservationRepository
                .findByUserId(user.getId())
                .stream()
                .anyMatch(r -> r.getVehicle().getId().equals(vehicleId)
                        && r.getStatus().name().equals("COMPLETED"));

        if (!hasCompletedReservation) {
            throw new IllegalStateException("You can only review vehicles you have rented");
        }

        // Create review
        Review review = new Review(vehicle, user, rating, comment);

        // Link to reservation if provided
        if (reservationId != null) {
            Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
            review.setReservation(reservation);
        }

        return reviewRepository.save(review);
    }

    /**
     * Get all reviews for a vehicle
     */
    public List<Review> getVehicleReviews(Long vehicleId) {
        return reviewRepository.findByVehicleIdOrderByCreatedDateDesc(vehicleId);
    }

    /**
     * Get all reviews by a user
     */
    public List<Review> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    /**
     * Calculate average rating for a vehicle
     */
    public Double getAverageRating(Long vehicleId) {
        Double average = reviewRepository.calculateAverageRating(vehicleId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    /**
     * Get review statistics for a vehicle
     */
    public ReviewStatistics getReviewStatistics(Long vehicleId) {
        List<Review> reviews = getVehicleReviews(vehicleId);

        if (reviews.isEmpty()) {
            return new ReviewStatistics(0.0, 0, 0, 0, 0, 0, 0);
        }

        int[] ratingCounts = new int[5]; // Index 0 = 1 star, Index 4 = 5 stars
        for (Review review : reviews) {
            ratingCounts[review.getRating() - 1]++;
        }

        Double avgRating = getAverageRating(vehicleId);

        return new ReviewStatistics(
                avgRating,
                reviews.size(),
                ratingCounts[4], // 5 stars
                ratingCounts[3], // 4 stars
                ratingCounts[2], // 3 stars
                ratingCounts[1], // 2 stars
                ratingCounts[0] // 1 star
        );
    }

    /**
     * Delete a review (only by owner or admin)
     */
    @Transactional
    public void deleteReview(Long reviewId, User currentUser) {
        Review review = reviewRepository.findById(
                Objects.requireNonNull(reviewId, "Review ID cannot be null"))
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // Only allow deletion by review owner or admin
        if (!review.getUser().getId().equals(currentUser.getId())
                && !currentUser.getRole().equals("ADMIN")) {
            throw new IllegalStateException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
    }

    /**
     * Update helpful count
     */
    @Transactional
    public void markAsHelpful(Long reviewId) {
        Review review = reviewRepository.findById(
                Objects.requireNonNull(reviewId, "Review ID cannot be null"))
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        reviewRepository.save(review);
    }

    /**
     * Inner class for review statistics
     */
    public static class ReviewStatistics {
        private final Double averageRating;
        private final Integer totalReviews;
        private final Integer fiveStars;
        private final Integer fourStars;
        private final Integer threeStars;
        private final Integer twoStars;
        private final Integer oneStar;

        public ReviewStatistics(Double averageRating, Integer totalReviews,
                Integer fiveStars, Integer fourStars, Integer threeStars,
                Integer twoStars, Integer oneStar) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
            this.fiveStars = fiveStars;
            this.fourStars = fourStars;
            this.threeStars = threeStars;
            this.twoStars = twoStars;
            this.oneStar = oneStar;
        }

        // Getters
        public Double getAverageRating() {
            return averageRating;
        }

        public Integer getTotalReviews() {
            return totalReviews;
        }

        public Integer getFiveStars() {
            return fiveStars;
        }

        public Integer getFourStars() {
            return fourStars;
        }

        public Integer getThreeStars() {
            return threeStars;
        }

        public Integer getTwoStars() {
            return twoStars;
        }

        public Integer getOneStar() {
            return oneStar;
        }
    }
}
