package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.util.ExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Import controller for Excel file upload
 */

@RestController
@RequestMapping("/api/v1/import")
@RequiredArgsConstructor
@Slf4j
public class ImportController {

    private final ExcelParser excelParser;

    /**
     * Upload and parse Excel file
     */
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadExcel(@RequestParam("file") MultipartFile file) {
        log.info("Uploading Excel file: {}", file.getOriginalFilename());

        // Validate file format
        if (!excelParser.validateFormat(file)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only .xlsx and .xls files are supported"));
        }

        try {
            // Parse Excel
            List<Map<String, Object>> data = excelParser.parse(file);

            Map<String, Object> result = Map.of(
                    "success", true,
                    "data", data,
                    "totalRows", data.size()
            );

            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (Exception e) {
            log.error("Error processing Excel file", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to process Excel file: " + e.getMessage()));
        }
    }

    /**
     * Validate imported data
     */
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateData(@RequestBody List<Map<String, Object>> data) {
        log.info("Validating {} rows of data", data.size());

        ExcelParser.ValidationResult result = excelParser.validate(data);

        Map<String, Object> response = Map.of(
                "valid", result.valid(),
                "errors", result.errors()
        );

        if (result.valid()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("Validation failed")
                            .data(response)
                            .build());
        }
    }
}
