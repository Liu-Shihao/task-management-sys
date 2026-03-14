package com.taskmanagement.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.entity.Task;
import com.taskmanagement.util.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * Jenkins task executor implementation
 */
@Slf4j
@Component
public class JenkinsExecutor implements TaskExecutor {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    @Value("${jenkins.url:}")
    private String jenkinsUrl;

    @Value("${jenkins.username:}")
    private String jenkinsUsername;

    @Value("${jenkins.api-token:}")
    private String jenkinsApiToken;

    @Value("${jenkins.timeout:300000}")
    private long timeout;

    @Override
    public ExecutionResult execute(Task task) {
        log.info("Executing Jenkins job for task: {}", task.getId());

        try {
            Map<String, Object> config = task.getConfig();
            String jobName = (String) config.get("jobName");
            @SuppressWarnings("unchecked")
            Map<String, String> parameters = (Map<String, String>) config.get("parameters");

            if (jobName == null || jobName.isEmpty()) {
                return ExecutionResult.failed("Job name is required");
            }

            // Build Jenkins URL
            String url = jenkinsUrl + "/job/" + jobName + "/buildWithParameters";
            if (parameters == null || parameters.isEmpty()) {
                url = jenkinsUrl + "/job/" + jobName + "/build";
            }

            // Create request body
            String requestBody = "";
            if (parameters != null && !parameters.isEmpty()) {
                requestBody = parameters.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .reduce((a, b) -> a + "&" + b)
                        .orElse("");
            }

            // Build request
            String auth = jenkinsUsername + ":" + (jenkinsApiToken != null ? EncryptionUtil.decrypt(jenkinsApiToken) : "");
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofMillis(timeout));

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            // Jenkins returns 201 for successful queue entry
            if (response.statusCode() == 201) {
                // Get queue location from response headers
                String queueLocation = response.headers().firstValue("Location").orElse("");
                String queueId = extractQueueId(queueLocation);
                
                String externalUrl = jenkinsUrl + "/job/" + jobName;
                
                // Build success result using builder
                return ExecutionResult.builder()
                        .success(true)
                        .message("Jenkins job triggered successfully")
                        .externalId(queueId)
                        .build();
            } else {
                log.error("Jenkins job trigger failed: {}", response.body());
                return ExecutionResult.failed("Jenkins trigger failed: " + response.statusCode());
            }

        } catch (Exception e) {
            log.error("Jenkins execution error", e);
            return ExecutionResult.failed("Execution error: " + e.getMessage());
        }
    }

    @Override
    public TaskStatus getStatus(String executionId) {
        log.info("Getting Jenkins status for: {}", executionId);

        try {
            // This is a simplified implementation
            // In production, need to query Jenkins queue/API for actual status
            return TaskStatus.builder()
                    .status("running")
                    .externalUrl(jenkinsUrl)
                    .build();

        } catch (Exception e) {
            log.error("Error getting Jenkins status", e);
            return TaskStatus.builder()
                    .status("unknown")
                    .build();
        }
    }

    @Override
    public boolean cancel(String executionId) {
        log.info("Cancelling Jenkins job: {}", executionId);
        // Jenkins doesn't support easy cancellation via API
        // Would need to implement via Jenkins CLI or StopBuildButton
        return false;
    }

    @Override
    public String getLogs(String executionId) {
        log.info("Getting Jenkins logs for: {}", executionId);
        // Implementation would fetch consoleOutput from Jenkins
        return "";
    }

    private String extractQueueId(String queueLocation) {
        if (queueLocation == null || queueLocation.isEmpty()) {
            return "";
        }
        // Extract queue ID from URL like: http://jenkins/queue/item/123/
        int lastSlash = queueLocation.lastIndexOf("/");
        if (lastSlash > 0) {
            String withoutTrailingSlash = queueLocation.endsWith("/") 
                ? queueLocation.substring(0, lastSlash) 
                : queueLocation;
            int secondLastSlash = withoutTrailingSlash.lastIndexOf("/");
            if (secondLastSlash > 0) {
                return withoutTrailingSlash.substring(secondLastSlash + 1);
            }
        }
        return queueLocation;
    }
}
