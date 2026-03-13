package com.taskmanagement.repository;

import com.taskmanagement.entity.TemplateTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TemplateTaskRepository extends JpaRepository<TemplateTask, Long> {
    List<TemplateTask> findByTemplateIdOrderByOrderIndexAsc(Long templateId);
    void deleteByTemplateId(Long templateId);
}
