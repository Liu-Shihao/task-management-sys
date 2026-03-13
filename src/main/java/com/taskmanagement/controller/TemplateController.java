package com.taskmanagement.controller;

import com.taskmanagement.dto.request.CreateTemplateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.request.GenerateRundownRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.RundownResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.TemplateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.service.TemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.web.PageableDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
 * Template management controller
 */

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {
    private static final Logger log = LoggerFactory.getLogger(TemplateController.class);

    private final TemplateService templateService;

    /**
     * Get all templates with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TemplateResponse>>> getTemplates(
            @PageableDefault(size = 20) Pageable pageable) {
        log.info("Getting templates, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<TemplateResponse> templates = templateService.getTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> getTemplate(@PathVariable Long id) {
        log.info("Getting template: {}", id);
        TemplateResponse template = templateService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    /**
     * Create new template
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Creating template: {}", request.getName());
        TemplateResponse template = templateService.createTemplate(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Template created", template));
    }

    /**
     * Clone template
     */
    @PostMapping("/{id}/clone")
    public ResponseEntity<ApiResponse<TemplateResponse>> cloneTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Cloning template: {}", id);
        String newName = body != null ? body.get("name") : null;
        TemplateResponse template = templateService.cloneTemplate(id, newName, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Template cloned", template));
    }

    /**
     * Generate rundown from template
     */
    @PostMapping("/{id}/generate-rundown")
    public ResponseEntity<ApiResponse<RundownResponse>> generateRundown(
            @PathVariable Long id,
            @RequestBody(required = false) GenerateRundownRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        log.info("Generating rundown from template: {}", id);
        if (request == null) {
            request = new GenerateRundownRequest();
        }
        RundownResponse rundown = templateService.generateRundown(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Rundown generated", rundown));
    }

    /**
     * Delete template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        log.info("Deleting template: {}", id);
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success("Template deleted", null));
    }
}
