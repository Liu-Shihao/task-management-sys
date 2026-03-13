package com.taskmanagement.dto.request;

/**
 * Generate rundown request DTO
 */
public class GenerateRundownRequest {

    private Long templateId;
    private String name;
    private String description;

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
