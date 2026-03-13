package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.dto.response.RundownResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.service.RundownService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.taskmanagement.service.TaskService;
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

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task controller
 */

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final RundownService rundownService;

    /**
     * Get tasks by rundown ID
     */
    @GetMapping("/rundowns/{rundownId}/tasks")
    public ResponseEntity<ApiResponse<List<RundownResponse.TaskResponse>>> getTasks(@PathVariable Long rundownId) {
        log.info("Getting tasks for rundown: {}", rundownId);
        List<Task> tasks = taskService.getTasksByRundown(rundownId);
        
        List<RundownResponse.TaskResponse> taskResponses = tasks.stream()
                .map(task -> RundownResponse.TaskResponse.builder()
                        .id(task.getId())
                        .name(task.getName())
                        .taskType(task.getTaskType())
                        .config(task.getConfig())
                        .orderIndex(task.getOrderIndex())
                        .status(task.getStatus())
                        .externalId(task.getExternalId())
                        .externalUrl(task.getExternalUrl())
                        .errorMessage(task.getErrorMessage())
                        .createdAt(task.getCreatedAt())
                        .startedAt(task.getStartedAt())
                        .finishedAt(task.getFinishedAt())
                        .build())
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(taskResponses));
    }

    /**
     * Get task by ID
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<RundownResponse.TaskResponse>> getTask(@PathVariable Long id) {
        log.info("Getting task: {}", id);
        Task task = taskService.getTask(id);
        
        RundownResponse.TaskResponse response = RundownResponse.TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .taskType(task.getTaskType())
                .config(task.getConfig())
                .orderIndex(task.getOrderIndex())
                .status(task.getStatus())
                .externalId(task.getExternalId())
                .externalUrl(task.getExternalUrl())
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .finishedAt(task.getFinishedAt())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Add task to rundown
     */
    @PostMapping("/rundowns/{rundownId}/tasks")
    public ResponseEntity<ApiResponse<RundownResponse>> addTask(
            @PathVariable Long rundownId,
            @RequestBody Map<String, Object> taskData) {
        log.info("Adding task to rundown: {}", rundownId);
        
        Task task = Task.builder()
                .name((String) taskData.get("name"))
                .taskType((String) taskData.get("taskType"))
                .config((Map<String, Object>) taskData.get("config"))
                .build();
        
        RundownResponse rundown = rundownService.addTask(rundownId, task);
        return ResponseEntity.ok(ApiResponse.success("Task added", rundown));
    }

    /**
     * Update task
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<RundownResponse.TaskResponse>> updateTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> taskData) {
        log.info("Updating task: {}", id);
        
        String name = (String) taskData.get("name");
        String taskType = (String) taskData.get("taskType");
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) taskData.get("config");
        
        Task task = taskService.updateTask(id, name, taskType, config);
        
        RundownResponse.TaskResponse response = RundownResponse.TaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .taskType(task.getTaskType())
                .config(task.getConfig())
                .orderIndex(task.getOrderIndex())
                .status(task.getStatus())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success("Task updated", response));
    }

    /**
     * Delete task
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        log.info("Deleting task: {}", id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }

    /**
     * Reorder tasks
     */
    @PutMapping("/rundowns/{rundownId}/tasks/reorder")
    public ResponseEntity<ApiResponse<RundownResponse>> reorderTasks(
            @PathVariable Long rundownId,
            @RequestBody Map<String, List<Long>> body) {
        log.info("Reordering tasks for rundown: {}", rundownId);
        
        List<Long> taskIds = body.get("taskIds");
        RundownResponse rundown = rundownService.reorderTasks(rundownId, taskIds);
        
        return ResponseEntity.ok(ApiResponse.success("Tasks reordered", rundown));
    }
}
