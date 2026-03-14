package com.taskmanagement.controller;

import com.taskmanagement.dto.response.ApiResponse;
import com.taskmanagement.entity.SystemConfig;
import com.taskmanagement.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * System configuration controller
 */

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final SystemConfigService configService;

    /**
     * Get all configs
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getAllConfigs() {
        log.info("Getting all configs");
        List<SystemConfig> configs = configService.getAllConfigs();
        
        // Mask sensitive values in response
        configs.forEach(config -> {
            if (isSensitiveKey(config.getConfigKey())) {
                config.setConfigValue("******");
            }
        });
        
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * Get config by key
     */
    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<SystemConfig>> getConfig(@PathVariable String key) {
        log.info("Getting config: {}", key);
        SystemConfig config = configService.getConfigEntity(key);
        
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Mask sensitive value
        if (isSensitiveKey(key)) {
            config.setConfigValue("******");
        }
        
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * Set config
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SystemConfig>> setConfig(@RequestBody Map<String, String> body) {
        String key = body.get("configKey");
        String value = body.get("configValue");
        String valueType = body.getOrDefault("valueType", "string");
        String description = body.get("description");

        log.info("Setting config: {}", key);
        
        if (key == null || key.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("configKey is required"));
        }
        
        if (value == null || value.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("configValue is required"));
        }

        SystemConfig config = configService.setConfig(key, value, valueType, description);
        return ResponseEntity.ok(ApiResponse.success("Config saved", config));
    }

    /**
     * Delete config
     */
    @DeleteMapping("/{key}")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable String key) {
        log.info("Deleting config: {}", key);
        configService.deleteConfig(key);
        return ResponseEntity.ok(ApiResponse.success("Config deleted", null));
    }

    private boolean isSensitiveKey(String key) {
        return key.contains("token") || 
               key.contains("password") || 
               key.contains("secret") ||
               key.contains("api_key");
    }
}
