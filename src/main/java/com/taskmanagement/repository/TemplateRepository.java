package com.taskmanagement.repository;

import com.taskmanagement.entity.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    Page<Template> findByCreatedBy(Long createdBy, Pageable pageable);
    Page<Template> findAll(Pageable pageable);
}
