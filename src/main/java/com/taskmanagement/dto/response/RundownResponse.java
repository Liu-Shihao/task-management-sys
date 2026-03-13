package com.taskmanagement.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rundown response DTO
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RundownResponse {

    private Long id;
    private String rundownCode;
    private String name;
    private String description;
    private Long templateId;
    private String status;
    private String scheduleType;
    private String cronExpression;
    private LocalDateTime runTime;
    private String scheduleStatus;
    private LocalDateTime nextRunTime;
    private LocalDateTime lastRunAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int taskCount;
    private List<TaskResponse> tasks;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRundownCode() { return rundownCode; }
    public void setRundownCode(String rundownCode) { this.rundownCode = rundownCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScheduleType() { return scheduleType; }
    public void setScheduleType(String scheduleType) { this.scheduleType = scheduleType; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public LocalDateTime getRunTime() { return runTime; }
    public void setRunTime(LocalDateTime runTime) { this.runTime = runTime; }

    public String getScheduleStatus() { return scheduleStatus; }
    public void setScheduleStatus(String scheduleStatus) { this.scheduleStatus = scheduleStatus; }

    public LocalDateTime getNextRunTime() { return nextRunTime; }
    public void setNextRunTime(LocalDateTime nextRunTime) { this.nextRunTime = nextRunTime; }

    public LocalDateTime getLastRunAt() { return lastRunAt; }
    public void setLastRunAt(LocalDateTime lastRunAt) { this.lastRunAt = lastRunAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getTaskCount() { return taskCount; }
    public void setTaskCount(int taskCount) { this.taskCount = taskCount; }

    public List<TaskResponse> getTasks() { return tasks; }
    public void setTasks(List<TaskResponse> tasks) { this.tasks = tasks; }

    public static RundownResponse fromEntity(com.taskmanagement.entity.Rundown rundown) {
        RundownResponse response = new RundownResponse();
        response.setId(rundown.getId());
        response.setRundownCode(rundown.getRundownCode());
        response.setName(rundown.getName());
        response.setDescription(rundown.getDescription());
        response.setTemplateId(rundown.getTemplateId());
        response.setStatus(rundown.getStatus());
        response.setScheduleType(rundown.getScheduleType());
        response.setCronExpression(rundown.getCronExpression());
        response.setRunTime(rundown.getRunTime());
        response.setScheduleStatus(rundown.getScheduleStatus());
        response.setNextRunTime(rundown.getNextRunTime());
        response.setLastRunAt(rundown.getLastRunAt());
        response.setCreatedBy(rundown.getCreatedBy());
        response.setCreatedAt(rundown.getCreatedAt());
        response.setUpdatedAt(rundown.getUpdatedAt());
        response.setTaskCount(rundown.getTasks() != null ? rundown.getTasks().size() : 0);
        
        if (rundown.getTasks() != null) {
            response.setTasks(rundown.getTasks().stream()
                .map(t -> TaskResponse.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .taskType(t.getTaskType())
                    .orderIndex(t.getOrderIndex())
                    .status(t.getStatus())
                    .build())
                .collect(Collectors.toList()));
        }
        
        return response;
    }

    public static class TaskResponse {
        private Long id;
        private String name;
        private String taskType;
        private Integer orderIndex;
        private String status;

        @Builder
        public TaskResponse(Long id, String name, String taskType, Integer orderIndex, String status) {
            this.id = id;
            this.name = name;
            this.taskType = taskType;
            this.orderIndex = orderIndex;
            this.status = status;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }

        public Integer getOrderIndex() { return orderIndex; }
        public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
