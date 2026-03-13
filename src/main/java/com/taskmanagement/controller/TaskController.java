package com.taskmanagement.controller;

import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import com.taskmanagement.service.TaskService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * 创建 Task。
     */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 根据 ID 查询 Task。
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        TaskResponse response = taskService.getTask(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 查询 Task 列表，可选按 rundownCode 过滤。
     */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(
            @RequestParam(value = "rundownCode", required = false) String rundownCode) {
        List<TaskResponse> responses = taskService.listTasks(rundownCode);
        return ResponseEntity.ok(responses);
    }

    /**
     * 更新 Task。
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id, @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除 Task。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 执行 Task（当前为同步模拟执行，直接标记为 COMPLETED）。
     */
    @PostMapping("/{id}/run")
    public ResponseEntity<TaskResponse> runTask(@PathVariable Long id) {
        TaskResponse response = taskService.runTask(id);
        return ResponseEntity.ok(response);
    }
}

