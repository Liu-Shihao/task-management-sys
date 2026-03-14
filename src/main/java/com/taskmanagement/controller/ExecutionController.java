package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Execution controller for triggering task/rundown execution
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExecutionController {

    private final ExecutionService executionService;

    /**
     * Execute a single task
     */
    @PostMapping("/tasks/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeTask(@PathVariable Long id) {
        log.info("Executing task: {}", id);
        executionService.executeTask(id);
        return ResponseEntity.accepted().body(ApiResponse.success("Task execution started", null));
    }

    /**
     * Execute a rundown
     */
    @PostMapping("/rundowns/{id}/execute")
    public ResponseEntity<ApiResponse<Void>> executeRundown(@PathVariable Long id) {
        log.info("Executing rundown: {}", id);
        executionService.executeRundown(id);
        return ResponseEntity.accepted().body(ApiResponse.success("Rundown execution started", null));
    }

    /**
     * Batch execute multiple rundowns
     */
    @PostMapping("/rundowns/batch-execute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> batchExecute(@RequestBody Map<String, List<Long>> body) {
        List<Long> rundownIds = body.get("rundownIds");

        if (rundownIds == null || rundownIds.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("rundownIds is required"));
        }

        log.info("Batch executing {} rundowns", rundownIds.size());

        // Execute each rundown asynchronously
        for (Long rundownId : rundownIds) {
            executionService.executeRundown(rundownId);
        }

        Map<String, Object> result = Map.of(
                "total", rundownIds.size(),
                "message", "Batch execution started"
        );

        return ResponseEntity.accepted().body(ApiResponse.success(result));
    }
}
