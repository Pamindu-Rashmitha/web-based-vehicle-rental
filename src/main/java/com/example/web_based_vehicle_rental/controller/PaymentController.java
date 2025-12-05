package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Payment;
import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.ReservationStatus;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import com.example.web_based_vehicle_rental.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final StripeService stripeService;
    private final ReservationRepository reservationRepository;

    @Autowired
    public PaymentController(StripeService stripeService, ReservationRepository reservationRepository) {
        this.stripeService = stripeService;
        this.reservationRepository = reservationRepository;
    }

    // Handle successful payment callback from Stripe

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam("session_id") String sessionId, Model model) {
        try {
            // Verify the payment with Stripe
            Payment payment = stripeService.verifyPayment(sessionId);

            // Update reservation status to CONFIRMED
            Reservation reservation = payment.getReservation();
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setPayment(payment);
            reservationRepository.save(reservation);

            // Add details to model for display
            model.addAttribute("reservation", reservation);
            model.addAttribute("payment", payment);
            model.addAttribute("success", true);

            return "payment_success";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to verify payment: " + e.getMessage());
            return "payment_success";
        }
    }

    // Handle cancelled payment

    @GetMapping("/cancel")
    public String paymentCancel(Model model) {
        model.addAttribute("message", "Payment was cancelled. You can try booking again.");
        return "payment_cancelled";
    }
}
