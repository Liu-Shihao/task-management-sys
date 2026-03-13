package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.service.ExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.service.SchedulerService;
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

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Execution controller for triggering task/rundown execution
 */

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExecutionController {
    private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);

    private final ExecutionService executionService;
    private final SchedulerService schedulerService;

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
     * Schedule rundown for one-time execution
     */
    @PostMapping("/rundowns/{id}/schedule/once")
    public ResponseEntity<ApiResponse<Void>> scheduleOnce(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String timeStr = body.get("runTime");
        LocalDateTime runTime = timeStr != null 
                ? LocalDateTime.parse(timeStr) 
                : LocalDateTime.now().plusMinutes(5);
        
        log.info("Scheduling rundown {} for {}", id, runTime);
        schedulerService.scheduleOnce(id, runTime);
        
        return ResponseEntity.ok(ApiResponse.success("Rundown scheduled", null));
    }

    /**
     * Schedule rundown for cron execution
     */
    @PostMapping("/rundowns/{id}/schedule/cron")
    public ResponseEntity<ApiResponse<Void>> scheduleCron(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        
        String cronExpression = body.get("cronExpression");
        if (cronExpression == null || cronExpression.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("cronExpression is required"));
        }
        
        log.info("Scheduling rundown {} for cron: {}", id, cronExpression);
        schedulerService.scheduleCron(id, cronExpression);
        
        return ResponseEntity.ok(ApiResponse.success("Rundown scheduled", null));
    }

    /**
     * Cancel scheduled execution
     */
    @PostMapping("/rundowns/{id}/schedule/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelSchedule(@PathVariable Long id) {
        log.info("Cancelling schedule for rundown: {}", id);
        schedulerService.cancelSchedule(id);
        return ResponseEntity.ok(ApiResponse.success("Schedule cancelled", null));
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
