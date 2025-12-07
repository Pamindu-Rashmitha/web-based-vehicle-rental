package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.Reservation;
import com.example.web_based_vehicle_rental.repository.ReservationRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Service
public class InvoiceService {

        @Autowired
        private ReservationRepository reservationRepository;

        private static final DeviceRgb ORANGE = new DeviceRgb(255, 123, 0);
        private static final DeviceRgb BLUE = new DeviceRgb(11, 61, 145);
        private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(248, 249, 250);

        /**
         * Generate PDF invoice for a reservation
         */
        public byte[] generateInvoice(Long reservationId) throws Exception {
                // Validate input to ensure type safety
                Objects.requireNonNull(reservationId, "Reservation ID cannot be null");

                Reservation reservation = reservationRepository.findById(reservationId)
                                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                // Company Header
                addHeader(document, reservation);

                // Invoice Details
                addInvoiceDetails(document, reservation);

                // Customer Details
                addCustomerDetails(document, reservation);

                // Booking Details Table
                addBookingTable(document, reservation);

                // Pricing Breakdown
                addPricingBreakdown(document, reservation);

                // Footer
                addFooter(document);

                document.close();
                return baos.toByteArray();
        }

        private void addHeader(Document document, Reservation reservation) {
                // Company Name
                Paragraph companyName = new Paragraph("DriveEase")
                                .setFontSize(28)
                                .setBold()
                                .setFontColor(BLUE)
                                .setTextAlignment(TextAlignment.CENTER);
                document.add(companyName);

                Paragraph tagline = new Paragraph("Vehicle Rental Services")
                                .setFontSize(12)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(30);
                document.add(tagline);

                // Invoice Title
                Paragraph invoiceTitle = new Paragraph("INVOICE")
                                .setFontSize(24)
                                .setBold()
                                .setFontColor(ORANGE)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginBottom(20);
                document.add(invoiceTitle);
        }

        private void addInvoiceDetails(Document document, Reservation reservation) {
                Table table = new Table(new float[] { 1, 1 });
                table.setWidth(UnitValue.createPercentValue(100));

                // Left side - Invoice info
                table.addCell(createInfoCell("Invoice #:", String.format("INV-%06d", reservation.getId()), false));
                table.addCell(
                                createInfoCell("Date:",
                                                LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                                true));
                table.addCell(createInfoCell("Status:", reservation.getStatus().toString(), false));
                table.addCell(createInfoCell("Payment Status:", "PAID", true));

                document.add(table);
                document.add(new Paragraph("\n"));
        }

        private void addCustomerDetails(Document document, Reservation reservation) {
                Paragraph customerTitle = new Paragraph("Bill To:")
                                .setFontSize(14)
                                .setBold()
                                .setFontColor(BLUE)
                                .setMarginBottom(10);
                document.add(customerTitle);

                Table customerTable = new Table(1);
                customerTable.setWidth(UnitValue.createPercentValue(100));
                customerTable.addCell(createDetailCell(reservation.getUser().getUsername()));
                customerTable.addCell(createDetailCell("Email: " + reservation.getUser().getEmail()));

                document.add(customerTable);
                document.add(new Paragraph("\n"));
        }

        private void addBookingTable(Document document, Reservation reservation) {
                Paragraph bookingTitle = new Paragraph("Booking Details:")
                                .setFontSize(14)
                                .setBold()
                                .setFontColor(BLUE)
                                .setMarginBottom(10);
                document.add(bookingTitle);

                Table table = new Table(new float[] { 3, 2, 1, 2 });
                table.setWidth(UnitValue.createPercentValue(100));

                // Header row
                table.addHeaderCell(createHeaderCell("Vehicle"));
                table.addHeaderCell(createHeaderCell("Period"));
                table.addHeaderCell(createHeaderCell("Days"));
                table.addHeaderCell(createHeaderCell("Amount"));

                // Data row
                String vehicleName = reservation.getVehicle().getBrand() + " " + reservation.getVehicle().getModel()
                                + " ("
                                + reservation.getVehicle().getYear() + ")";
                String period = formatDate(reservation.getStartDate()) + " to " + formatDate(reservation.getEndDate());
                long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
                String amount = "LKR " + String.format("%.2f", reservation.getTotalPrice());

                table.addCell(createDataCell(vehicleName));
                table.addCell(createDataCell(period));
                table.addCell(createDataCell(String.valueOf(days)));
                table.addCell(createDataCell(amount));

                document.add(table);
                document.add(new Paragraph("\n"));
        }

        private void addPricingBreakdown(Document document, Reservation reservation) {
                Table table = new Table(new float[] { 3, 1 });
                table.setWidth(UnitValue.createPercentValue(100));

                long days = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
                double dailyRate = reservation.getVehicle().getDailyPrice();
                double subtotal = dailyRate * days;
                double tax = subtotal * 0.15; // 15% tax
                double total = reservation.getTotalPrice();

                // Subtotal
                table.addCell(createPriceCell("Subtotal:", false));
                table.addCell(createPriceCell("LKR " + String.format("%.2f", subtotal), false));

                // Tax
                table.addCell(createPriceCell("Tax (15%):", false));
                table.addCell(createPriceCell("LKR " + String.format("%.2f", tax), false));

                // Total with colored background
                Cell totalLabelCell = new Cell()
                                .add(new Paragraph("Total Amount:").setBold().setFontSize(14))
                                .setBackgroundColor(LIGHT_GRAY)
                                .setPadding(10)
                                .setBorder(null);

                Cell totalValueCell = new Cell()
                                .add(new Paragraph("LKR " + String.format("%.2f", total)).setBold().setFontSize(14)
                                                .setFontColor(ORANGE))
                                .setBackgroundColor(LIGHT_GRAY)
                                .setPadding(10)
                                .setBorder(null)
                                .setTextAlignment(TextAlignment.RIGHT);

                table.addCell(totalLabelCell);
                table.addCell(totalValueCell);

                document.add(table);
                document.add(new Paragraph("\n\n"));
        }

        private void addFooter(Document document) {
                Paragraph thankYou = new Paragraph("Thank you for choosing DriveEase!")
                                .setFontSize(12)
                                .setBold()
                                .setFontColor(BLUE)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginTop(20);
                document.add(thankYou);

                Paragraph contact = new Paragraph("For support: support@driveease.com | +94 11 234 5678")
                                .setFontSize(10)
                                .setFontColor(ColorConstants.GRAY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginTop(5);
                document.add(contact);

                Paragraph terms = new Paragraph("Terms and Conditions Apply | All payments are non-refundable")
                                .setFontSize(8)
                                .setFontColor(ColorConstants.LIGHT_GRAY)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setMarginTop(30);
                document.add(terms);
        }

        // Helper methods for creating cells
        private Cell createInfoCell(String label, String value, boolean rightAlign) {
                Paragraph p = new Paragraph()
                                .add(new Paragraph(label).setBold().setFontSize(10))
                                .add(" " + value);

                Cell cell = new Cell()
                                .add(p)
                                .setBorder(null)
                                .setPadding(5);

                if (rightAlign) {
                        cell.setTextAlignment(TextAlignment.RIGHT);
                }

                return cell;
        }

        private Cell createDetailCell(String text) {
                return new Cell()
                                .add(new Paragraph(text).setFontSize(10))
                                .setBorder(null)
                                .setPadding(3);
        }

        private Cell createHeaderCell(String text) {
                return new Cell()
                                .add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE))
                                .setBackgroundColor(BLUE)
                                .setPadding(10)
                                .setTextAlignment(TextAlignment.CENTER);
        }

        private Cell createDataCell(String text) {
                return new Cell()
                                .add(new Paragraph(text).setFontSize(10))
                                .setPadding(10)
                                .setTextAlignment(TextAlignment.LEFT);
        }

        private Cell createPriceCell(String text, boolean isBold) {
                Paragraph p = new Paragraph(text).setFontSize(11);
                if (isBold)
                        p.setBold();

                return new Cell()
                                .add(p)
                                .setBorder(null)
                                .setPadding(5)
                                .setTextAlignment(text.startsWith("LKR") ? TextAlignment.RIGHT : TextAlignment.LEFT);
        }

        private String formatDate(LocalDate date) {
                return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
}
