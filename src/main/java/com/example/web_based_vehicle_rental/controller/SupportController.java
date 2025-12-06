package com.example.web_based_vehicle_rental.controller;

import com.example.web_based_vehicle_rental.model.SupportCategory;
import com.example.web_based_vehicle_rental.model.SupportRequest;
import com.example.web_based_vehicle_rental.model.SupportStatus;
import com.example.web_based_vehicle_rental.model.User;
import com.example.web_based_vehicle_rental.service.SupportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SupportController {

    private final SupportService supportService;

    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

    @GetMapping("/support")
    public String supportPage(Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("categories", SupportCategory.values());
        model.addAttribute("userRequests", supportService.getUserRequests(user));
        return "support";
    }

    @PostMapping("/api/support/request")
    @ResponseBody
    public ResponseEntity<?> submitSupportRequest(
            @RequestParam("category") SupportCategory category,
            @RequestParam("subject") String subject,
            @RequestParam("description") String description,
            @RequestParam(value = "screenshot", required = false) MultipartFile screenshot,
            @AuthenticationPrincipal User user) {

        try {
            SupportRequest request = supportService.createSupportRequest(
                    user, category, subject, description, screenshot);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Support request submitted successfully!");
            response.put("requestId", request.getId());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to upload screenshot: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to submit request: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/api/support/my-requests")
    @ResponseBody
    public ResponseEntity<List<SupportRequest>> getMyRequests(@AuthenticationPrincipal User user) {
        List<SupportRequest> requests = supportService.getUserRequests(user);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/api/admin/support/requests")
    @ResponseBody
    public ResponseEntity<List<SupportRequest>> getAllRequests() {
        List<SupportRequest> requests = supportService.getAllRequests();
        return ResponseEntity.ok(requests);
    }

    @PostMapping("/api/admin/support/update-status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(
            @RequestParam("requestId") Long requestId,
            @RequestParam("status") SupportStatus status) {

        try {
            SupportRequest updated = supportService.updateStatus(requestId, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("request", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
