package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.dto.response.RundownResponse;
import com.taskmanagement.service.RundownService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Rundown management controller
 */

@RestController
@RequestMapping("/api/v1/rundowns")
@RequiredArgsConstructor
@Slf4j
public class RundownController {

    private final RundownService rundownService;

    /**
     * Get all rundowns with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RundownResponse>>> getRundowns(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting rundowns, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<RundownResponse> rundowns = rundownService.getRundowns(pageable);
        return ResponseEntity.ok(ApiResponse.success(rundowns));
    }

    /**
     * Get rundown by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RundownResponse>> getRundown(@PathVariable Long id) {
        log.info("Getting rundown: {}", id);
        RundownResponse rundown = rundownService.getRundown(id);
        return ResponseEntity.ok(ApiResponse.success(rundown));
    }

    /**
     * Get rundown by code
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<RundownResponse>> getRundownByCode(@PathVariable String code) {
        log.info("Getting rundown by code: {}", code);
        RundownResponse rundown = rundownService.getRundownByCode(code);
        return ResponseEntity.ok(ApiResponse.success(rundown));
    }

    /**
     * Create blank rundown
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RundownResponse>> createRundown(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Creating rundown: {}", body.get("name"));
        
        String name = body.get("name");
        String description = body.get("description");
        
        RundownResponse rundown = rundownService.createRundown(name, description, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Rundown created", rundown));
    }

    /**
     * Update rundown
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RundownResponse>> updateRundown(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.info("Updating rundown: {}", id);
        
        String name = body.get("name");
        String description = body.get("description");
        
        RundownResponse rundown = rundownService.updateRundown(id, name, description);
        return ResponseEntity.ok(ApiResponse.success("Rundown updated", rundown));
    }

    /**
     * Delete rundown
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRundown(@PathVariable Long id) {
        log.info("Deleting rundown: {}", id);
        rundownService.deleteRundown(id);
        return ResponseEntity.ok(ApiResponse.success("Rundown deleted", null));
    }
}
