package com.taskmanagement.repository;

import com.taskmanagement.entity.UploadLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadLogRepository extends JpaRepository<UploadLog, Long> {
}

