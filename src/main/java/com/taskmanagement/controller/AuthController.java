package com.taskmanagement.controller;

import com.taskmanagement.dto.request.LoginRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication controller
 */

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK"));
    }

    /**
     * Login endpoint (simplified for V1.0)
     * In production, implement proper JWT or Session-based auth
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());
        
        // Simplified login response - implement full auth in production
        Map<String, Object> data = Map.of(
                "userId", 1L,
                "username", request.getUsername(),
                "role", "user",
                "token", "demo-token-" + System.currentTimeMillis()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Login successful", data));
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("User logged out");
        return ResponseEntity.ok(ApiResponse.success("Logout successful", null));
    }
}
