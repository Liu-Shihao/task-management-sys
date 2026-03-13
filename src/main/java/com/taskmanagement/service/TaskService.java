package com.taskmanagement.service;

import com.taskmanagement.dto.TaskRequest;
import com.taskmanagement.dto.TaskResponse;
import java.util.List;

public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    TaskResponse updateTask(Long id, TaskRequest request);

    TaskResponse getTask(Long id);

    List<TaskResponse> listTasks(String rundownCode);

    void deleteTask(Long id);

    /**
     * 执行任务：目前只是模拟执行，立即将状态置为 COMPLETED。
     * 后续可以在这里对接 Jenkins / Ansible。
     */
    TaskResponse runTask(Long id);
}

