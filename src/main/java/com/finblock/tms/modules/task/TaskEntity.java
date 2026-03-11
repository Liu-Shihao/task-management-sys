package com.finblock.tms.modules.task;

import com.finblock.tms.common.model.CreatedUpdatedAtEntity;
import com.finblock.tms.modules.rundown.ReleaseRundownEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "task")
public class TaskEntity extends CreatedUpdatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rundown_id", nullable = false)
    private ReleaseRundownEntity rundown;

    @Column(nullable = false)
    private String name;

    @Column(name = "params", nullable = false, columnDefinition = "json")
    private String paramsJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "external_job_url")
    private String externalJobUrl;

    public ReleaseRundownEntity getRundown() {
        return rundown;
    }

    public void setRundown(ReleaseRundownEntity rundown) {
        this.rundown = rundown;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getExternalJobUrl() {
        return externalJobUrl;
    }

    public void setExternalJobUrl(String externalJobUrl) {
        this.externalJobUrl = externalJobUrl;
    }

}
