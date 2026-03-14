package com.taskmanagement.service;

import com.taskmanagement.entity.SystemConfig;
import com.taskmanagement.exception.BusinessException;
import com.taskmanagement.repository.SystemConfigRepository;
import com.taskmanagement.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * System configuration service
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigService {

    private final SystemConfigRepository configRepository;

    /**
     * Get all configs
     */
    @Transactional(readOnly = true)
    public List<SystemConfig> getAllConfigs() {
        return configRepository.findAll();
    }

    /**
     * Get config by key
     */
    @Transactional(readOnly = true)
    public String getConfig(String key) {
        return configRepository.findByConfigKey(key)
                .map(config -> {
                    // Decrypt sensitive values
                    if (isSensitiveKey(key) && EncryptionUtil.isEncrypted(config.getConfigValue())) {
                        return EncryptionUtil.decrypt(config.getConfigValue());
                    }
                    return config.getConfigValue();
                })
                .orElse(null);
    }

    /**
     * Get config by key (with encryption check)
     */
    @Transactional(readOnly = true)
    public SystemConfig getConfigEntity(String key) {
        return configRepository.findByConfigKey(key).orElse(null);
    }

    /**
     * Set config
     */
    @Transactional
    public SystemConfig setConfig(String key, String value, String valueType, String description) {
        log.info("Setting config: {}", key);

        SystemConfig config = configRepository.findByConfigKey(key)
                .orElse(SystemConfig.builder().configKey(key).build());

        // Encrypt sensitive values
        if (isSensitiveKey(key) && value != null && !EncryptionUtil.isEncrypted(value)) {
            value = EncryptionUtil.encrypt(value);
        }

        config.setConfigValue(value);
        
        if (valueType != null) {
            config.setValueType(valueType);
        }
        if (description != null) {
            config.setDescription(description);
        }

        return configRepository.save(config);
    }

    /**
     * Delete config
     */
    @Transactional
    public void deleteConfig(String key) {
        log.info("Deleting config: {}", key);
        configRepository.findByConfigKey(key).ifPresent(configRepository::delete);
    }

    /**
     * Check if key is sensitive (should be encrypted)
     */
    private boolean isSensitiveKey(String key) {
        return key.contains("token") || 
               key.contains("password") || 
               key.contains("secret") ||
               key.contains("api_key");
    }
}
