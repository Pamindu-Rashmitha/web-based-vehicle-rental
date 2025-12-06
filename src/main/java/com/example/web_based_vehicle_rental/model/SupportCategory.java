package com.example.web_based_vehicle_rental.model;

public enum SupportCategory {
    BOOKING_ISSUE("Booking Issue"),
    PAYMENT_PROBLEM("Payment Problem"),
    VEHICLE_CONCERN("Vehicle Concern"),
    ACCOUNT_HELP("Account Help"),
    OTHER("Other");

    private final String displayName;

    SupportCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
