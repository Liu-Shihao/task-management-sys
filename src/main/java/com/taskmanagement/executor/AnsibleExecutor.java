package com.taskmanagement.executor;

import com.taskmanagement.entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ansible (AWX/Tower) task executor implementation
 */

@Component
public class AnsibleExecutor implements TaskExecutor {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${ansible.awx-url:}")
    private String awxUrl;

    @Value("${ansible.awx-token:}")
    private String awxToken;

    @Value("${ansible.timeout:600000}")
    private long timeout;

    @Override
    public ExecutionResult execute(Task task) {
        log.info("Executing Ansible playbook for task: {}", task.getId());

        try {
            Map<String, Object> config = task.getConfig();
            String playbook = (String) config.get("playbook");
            Map<String, Object> extraVars = (Map<String, Object>) config.get("extra_vars");
            Integer jobTemplateId = (Integer) config.get("jobTemplateId");

            String url;
            if (jobTemplateId != null) {
                // Launch via job template
                url = awxUrl + "/api/v2/job_templates/" + jobTemplateId + "/launch/";
            } else if (playbook != null) {
                // Launch ad-hoc job (simplified)
                url = awxUrl + "/api/v2/jobs/";
            } else {
                return ExecutionResult.failed("Playbook or Job Template is required");
            }

            // Build request body
            String requestBody = "{}";
            if (extraVars != null && !extraVars.isEmpty()) {
                requestBody = String.format("{\"extra_vars\": %s}", extraVars);
            }

            // Build request
            String encodedToken = Base64.getEncoder().encodeToString(("Bearer:" + awxToken).getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + awxToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofMillis(timeout))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                // Parse response to get job ID
                String jobId = extractJobId(response.body());
                String externalUrl = awxUrl + "/api/v2/jobs/" + jobId + "/";
                return ExecutionResult.success(jobId, externalUrl, null);
            } else {
                log.error("Ansible AWX job trigger failed: {}", response.body());
                return ExecutionResult.failed("AWX trigger failed: " + response.statusCode());
            }

        } catch (Exception e) {
            log.error("Ansible execution error", e);
            return ExecutionResult.failed("Execution error: " + e.getMessage());
        }
    }

    @Override
    public TaskStatus getStatus(String executionId) {
        log.info("Getting Ansible AWX status for: {}", executionId);

        try {
            String url = awxUrl + "/api/v2/jobs/" + executionId + "/";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + awxToken)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse JSON to get status
                String status = parseStatus(response.body());
                String output = parseOutput(response.body());

                return TaskStatus.builder()
                        .status(mapAnsibleStatus(status))
                        .externalId(executionId)
                        .externalUrl(awxUrl + "/api/v2/jobs/" + executionId + "/")
                        .output(output)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error getting Ansible AWX status", e);
        }

        return TaskStatus.builder()
                .status("unknown")
                .build();
    }

    @Override
    public boolean cancel(String executionId) {
        log.info("Cancelling Ansible AWX job: {}", executionId);

        try {
            String url = awxUrl + "/api/v2/jobs/" + executionId + "/cancel/";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + awxToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 202;

        } catch (Exception e) {
            log.error("Error cancelling Ansible AWX job", e);
            return false;
        }
    }

    @Override
    public String getLogs(String executionId) {
        log.info("Getting Ansible AWX logs for: {}", executionId);

        try {
            String url = awxUrl + "/api/v2/jobs/" + executionId + "/stdout/?format=txt";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + awxToken)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            log.error("Error getting Ansible AWX logs", e);
            return "";
        }
    }

    private String extractJobId(String responseBody) {
        // Simple JSON parsing - in production use ObjectMapper
        try {
            int start = responseBody.indexOf("\"id\":");
            if (start > 0) {
                int end = responseBody.indexOf(",", start);
                return responseBody.substring(start + 4, end).trim();
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    private String parseStatus(String responseBody) {
        try {
            int start = responseBody.indexOf("\"status\":\"");
            if (start > 0) {
                int end = responseBody.indexOf("\"", start + 11);
                return responseBody.substring(start + 10, end);
            }
        } catch (Exception e) {
            // ignore
        }
        return "unknown";
    }

    private String parseOutput(String responseBody) {
        // Simplified - would need to fetch stdout separately
        return "";
    }

    private String mapAnsibleStatus(String awxStatus) {
        return switch (awxStatus.toLowerCase()) {
            case "pending", "waiting", "running" -> "running";
            case "successful" -> "success";
            case "failed", "error" -> "failed";
            case "canceled" -> "aborted";
            default -> "unknown";
        };
    }
}
