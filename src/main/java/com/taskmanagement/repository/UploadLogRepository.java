package com.taskmanagement.repository;

import com.taskmanagement.entity.UploadLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadLogRepository extends JpaRepository<UploadLog, Long> {
}
