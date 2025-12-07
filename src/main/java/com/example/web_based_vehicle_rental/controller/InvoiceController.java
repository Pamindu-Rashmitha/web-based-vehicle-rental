package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import com.example.web_based_vehicle_rental.repository.UserRepository;
import com.example.web_based_vehicle_rental.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/invoices")
public class InvoiceController {

        @Autowired
        private InvoiceService invoiceService;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private UserRepository userRepository;

        /**
         * Download invoice PDF for a reservation
         */
        @GetMapping("/{reservationId}/download")
        public ResponseEntity<?> downloadInvoice(@PathVariable Long reservationId,
                        Authentication authentication) {
                try {
                        // Validate reservationId
                        if (reservationId == null) {
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                                .body("Reservation ID is required");
                        }

                        // Get current user
                        String username = authentication.getName();
                        User user = userRepository.findByUsername(username)
                                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

                        // Get reservation
                        Reservation reservation = reservationRepository.findById(reservationId)
                                        .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

                        // Security check: user can only download their own invoices (or admin can
                        // download any)
                        if (!reservation.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
                                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                                .body("You can only download your own invoices");
                        }

                        // Generate PDF
                        byte[] pdfBytes = invoiceService.generateInvoice(reservationId);

                        // Set headers
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_PDF);
                        headers.setContentDispositionFormData("attachment",
                                        String.format("Invoice-INV-%06d.pdf", reservationId));
                        headers.setContentLength(pdfBytes.length);

                        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

                } catch (IllegalArgumentException e) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(e.getMessage());
                } catch (Exception e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error generating invoice: " + e.getMessage());
                }
        }
}
