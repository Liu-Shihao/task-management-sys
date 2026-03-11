package com.finblock.tms.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Auth auth,
        Upload upload,
        Integrations integrations
) {
    public record Auth(@NotBlank String headerName) {}

    public record Upload(
            @NotBlank String storageDir,
            @Min(1) long maxSizeBytes
    ) {}

    public record Integrations(Jenkins jenkins, Ansible ansible) {
        public record Jenkins(@NotBlank String baseUrl, String username, String apiToken) {}

        public record Ansible(@NotBlank String baseUrl, String apiToken) {}
    }
}

