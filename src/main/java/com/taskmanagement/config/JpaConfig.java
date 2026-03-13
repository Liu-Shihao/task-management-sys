package com.taskmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.context.annotation.Bean;
import java.util.Optional;

/**
 * JPA configuration with auditing support
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        // In production, get current user from security context
        return () -> Optional.of(1L); // Default to system user
    }
}
