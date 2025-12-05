package com.example.web_based_vehicle_rental.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Brand cannot be empty")
    private String brand;

    @NotEmpty(message = "Model cannot be empty")
    private String model;

    @Min(value = 1900, message = "Year must be valid")
    private int year;

    @NotEmpty(message = "License plate cannot be empty")
    @Column(unique = true)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    private VehicleStatus status;

    @NotEmpty(message = "Type cannot be empty")
    private String type;

    private String imageUrl;

    @NotNull(message = "Daily price cannot be null")
    @Min(value = 0, message = "Price must be positive")
    private Double dailyPrice;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<VehicleImage> images = new ArrayList<>();

    public Vehicle() {
        this.status = VehicleStatus.AVAILABLE;
        this.images = new ArrayList<>();
    }

    public Vehicle(String brand, String model, int year, String licensePlate, Double dailyPrice, String type) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.licensePlate = licensePlate;
        this.dailyPrice = dailyPrice;
        this.type = type;
        this.status = VehicleStatus.AVAILABLE;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getDailyPrice() {
        return dailyPrice;
    }

    public void setDailyPrice(Double dailyPrice) {
        this.dailyPrice = dailyPrice;
    }

    public List<VehicleImage> getImages() {
        return images;
    }

    public void setImages(List<VehicleImage> images) {
        this.images = images;
    }

    public void addImage(VehicleImage image) {
        images.add(image);
        image.setVehicle(this);
    }

    public void removeImage(VehicleImage image) {
        images.remove(image);
        image.setVehicle(null);
    }

    // Helper method to get primary image URL for backward compatibility
    @com.fasterxml.jackson.annotation.JsonProperty("primaryImageUrl")
    public String getPrimaryImageUrl() {
        try {
            if (images != null && !images.isEmpty()) {
                return images.stream()
                        .filter(VehicleImage::getIsPrimary)
                        .findFirst()
                        .map(VehicleImage::getImageUrl)
                        .orElse(imageUrl);
            }
        } catch (Exception e) {
            // Handle lazy loading exception gracefully
        }
        return imageUrl; // Fallback to old imageUrl field
    }
}
