package com.example.web_based_vehicle_rental.service;

import com.example.web_based_vehicle_rental.model.SupportCategory;
import com.example.web_based_vehicle_rental.model.SupportRequest;
import com.example.web_based_vehicle_rental.model.SupportStatus;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.repository.SupportRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class SupportService {

    private final SupportRequestRepository supportRequestRepository;
    private static final String UPLOAD_DIR = "uploads/support-screenshots/";

    public SupportService(SupportRequestRepository supportRequestRepository) {
        this.supportRequestRepository = supportRequestRepository;
        // Create upload directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public SupportRequest createSupportRequest(User user, SupportCategory category,
            String subject, String description,
            MultipartFile screenshot) throws IOException {
        SupportRequest request = new SupportRequest(user, category, subject, description);

        // Handle screenshot upload if provided
        if (screenshot != null && !screenshot.isEmpty()) {
            String screenshotUrl = saveScreenshot(screenshot);
            request.setScreenshotUrl(screenshotUrl);
        }

        return supportRequestRepository.save(request);
    }

    private String saveScreenshot(MultipartFile file) throws IOException {
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = Paths.get(UPLOAD_DIR + filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for storage and access
        return "/uploads/support-screenshots/" + filename;
    }

    public List<SupportRequest> getUserRequests(User user) {
        return supportRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<SupportRequest> getAllRequests() {
        return supportRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SupportRequest> getRequestsByStatus(SupportStatus status) {
        return supportRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public SupportRequest updateStatus(Long requestId, SupportStatus newStatus) {
        if (requestId == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        SupportRequest request = supportRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Support request not found"));
        request.setStatus(newStatus);
        return supportRequestRepository.save(request);
    }

    public long countOpenRequests(User user) {
        return supportRequestRepository.countByUserAndStatus(user, SupportStatus.OPEN);
    }

    public long countAllOpenRequests() {
        return supportRequestRepository.countByStatus(SupportStatus.OPEN);
    }

    public SupportRequest getRequestById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Request ID cannot be null");
        }
        return supportRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Support request not found"));
    }
}
