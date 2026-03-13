package com.taskmanagement.dto;

import com.taskmanagement.enums.TaskStatus;
import com.taskmanagement.enums.TaskType;
import java.time.Instant;

/**
 * 用于创建 / 更新 Task 的请求对象。
 * 主要面向前端表单和 API 交互。
 */
public class TaskRequest {

    private String rundownCode;
    private String rundownName;
    private Integer sequenceNo;
    private String name;
    private String description;

    private TaskStatus status;
    private TaskType automationType;
    private String automationTarget;
    private String automationParamsJson;

    private Boolean schedulingEnabled;
    private Instant scheduledTime;
    private String cronExpression;

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

    public Boolean getSchedulingEnabled() {
        return schedulingEnabled;
    }

    public void setSchedulingEnabled(Boolean schedulingEnabled) {
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
}

