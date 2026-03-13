package com.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API error response format.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private String code;

    private String message;

    private String details;

    private String path;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant timestamp;
}