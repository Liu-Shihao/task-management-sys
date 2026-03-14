package com.taskmanagement;

import com.taskmanagement.entity.User;
import com.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class TaskManagementApplication {

    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(TaskManagementApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository) {
        return args -> {
            // Create default admin user if not exists
            if (!userRepository.existsByUsername("admin")) {
                User admin = User.builder()
                        .username("admin")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .displayName("System Administrator")
                        .role("admin")
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Created default admin user (username: admin, password: admin123)");
            }

            // Create default user if not exists
            if (!userRepository.existsByUsername("user")) {
                User user = User.builder()
                        .username("user")
                        .passwordHash(passwordEncoder.encode("user123"))
                        .displayName("Default User")
                        .role("user")
                        .enabled(true)
                        .build();
                userRepository.save(user);
                log.info("Created default user (username: user, password: user123)");
            }
        };
    }
}
