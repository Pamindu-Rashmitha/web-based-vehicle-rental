package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.Payment;
import com.example.web_based_vehicle_rental.model.PaymentStatus;
import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private final PaymentRepository paymentRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    @Value("${stripe.cancellation.fee.percentage:10}")
    private Integer cancellationFeePercentage;

    public StripeService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Create a Stripe Checkout Session for a reservation

    public String createCheckoutSession(Reservation reservation, String successUrl, String cancelUrl)
            throws StripeException {
        Stripe.apiKey = stripeApiKey;

        // Convert amount to cents
        long amountInCents = (long) (reservation.getTotalPrice() * 100);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Vehicle Rental: "
                                                                        + reservation.getVehicle().getBrand() + " "
                                                                        + reservation.getVehicle().getModel())
                                                                .setDescription(
                                                                        "Rental from " + reservation.getStartDate()
                                                                                + " to " + reservation.getEndDate())
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("reservationId", reservation.getId().toString())
                .putMetadata("vehicleId", reservation.getVehicle().getId().toString())
                .putMetadata("userId", reservation.getUser().getId().toString())
                .build();

        Session session = Session.create(params);

        // Create payment record with PENDING status
        Payment payment = new Payment(reservation, reservation.getTotalPrice(), session.getId());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return session.getUrl();
    }

    // Verify payment and update payment record

    public Payment verifyPayment(String sessionId) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        Session session = Session.retrieve(sessionId);

        Payment payment = paymentRepository.findByStripeSessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Payment not found for session: " + sessionId));

        if ("paid".equals(session.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setStripePaymentIntentId(session.getPaymentIntent());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        return paymentRepository.save(payment);
    }

    // Process refund with cancellation fee

    public Payment processRefund(Payment payment) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        if (payment.getStripePaymentIntentId() == null) {
            throw new RuntimeException("Cannot refund: No payment intent found");
        }

        // Calculate refund amount (total - cancellation fee)
        double feeAmount = payment.getAmount() * (cancellationFeePercentage / 100.0);
        double refundAmount = payment.getAmount() - feeAmount;
        long refundAmountInCents = (long) (refundAmount * 100);

        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(payment.getStripePaymentIntentId())
                .setAmount(refundAmountInCents)
                .build();

        Refund refund = Refund.create(params);

        payment.setRefundAmount(refundAmount);
        payment.setStripeRefundId(refund.getId());
        payment.setStatus(PaymentStatus.REFUNDED);

        return paymentRepository.save(payment);
    }

    // Get publishable key for frontend
    public String getPublishableKey() {
        return stripePublishableKey;
    }

    // Get cancellation fee percentage
    public Integer getCancellationFeePercentage() {
        return cancellationFeePercentage;
    }
}
