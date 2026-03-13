package com.taskmanagement.entity;

import com.taskmanagement.enums.UploadStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Excel文件上传记录实体
 * 用于记录用户上传Excel文件的日志信息
 */
@Entity
@Table(name = "upload_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 上传的文件名称
     */
    @Column(nullable = false, length = 255)
    private String fileName;


    /**
     * 文件存储路径（相对路径或完整路径）
     */
    @Column(length = 500)
    private String filePath;

    /**
     * 文件大小（字节）
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * 文件内容类型（MIME类型）
     */
    @Column(length = 100)
    private String contentType;

    /**
     * 上传状态：SUCCESS（成功）、FAILED（失败）
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UploadStatus status;

    /**
     * 处理结果消息（成功或失败的描述信息）
     */
    @Column(length = 2000)
    private String message;

    @Column(length = 2000)
    private String errorMessage;

    /**
     * 成功导入的任务数量
     */
    @Column
    private Integer successCount = 0;

    /**
     * 导入失败的任务数量
     */
    @Column
    private Integer failCount = 0;

    /**
     * 上传用户标识
     */
    @Column(length = 100)
    private String uploadedBy;

    /**
     * 处理完成时间
     */
    private Instant processedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = UploadStatus.SUCCESS;
        }
        if (this.successCount == null) {
            this.successCount = 0;
        }
        if (this.failCount == null) {
            this.failCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }


}
