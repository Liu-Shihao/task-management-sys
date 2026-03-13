package com.taskmanagement.dto;

import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.enums.TaskType;
import java.time.Instant;

/**
 * Task 的对外返回对象。
 */
public class TaskResponse {

    private Long id;

    private String rundownCode;
    private String rundownName;
    private Integer sequenceNo;
    private String name;
    private String description;

    private TaskStatus status;
    private TaskType automationType;
    private String automationTarget;
    private String automationParamsJson;

    private boolean schedulingEnabled;
    private Instant scheduledTime;
    private String cronExpression;
    private Instant nextRunAt;
    private Instant lastScheduledRunAt;

    private Instant startedAt;
    private Instant finishedAt;

    private String externalExecutionId;
    private String externalUrl;
    private String lastMessage;

    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRundownCode() {
        return rundownCode;
    }

    public void setRundownCode(String rundownCode) {
        this.rundownCode = rundownCode;
    }

    public String getRundownName() {
        return rundownName;
    }

    public void setRundownName(String rundownName) {
        this.rundownName = rundownName;
    }

    public Integer getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getAutomationType() {
        return automationType;
    }

    public void setAutomationType(TaskType automationType) {
        this.automationType = automationType;
    }

    public String getAutomationTarget() {
        return automationTarget;
    }

    public void setAutomationTarget(String automationTarget) {
        this.automationTarget = automationTarget;
    }

    public String getAutomationParamsJson() {
        return automationParamsJson;
    }

    public void setAutomationParamsJson(String automationParamsJson) {
        this.automationParamsJson = automationParamsJson;
    }

    public boolean isSchedulingEnabled() {
        return schedulingEnabled;
    }

    public void setSchedulingEnabled(boolean schedulingEnabled) {
        this.schedulingEnabled = schedulingEnabled;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public Instant getLastScheduledRunAt() {
        return lastScheduledRunAt;
    }

    public void setLastScheduledRunAt(Instant lastScheduledRunAt) {
        this.lastScheduledRunAt = lastScheduledRunAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getExternalExecutionId() {
        return externalExecutionId;
    }

    public void setExternalExecutionId(String externalExecutionId) {
        this.externalExecutionId = externalExecutionId;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

