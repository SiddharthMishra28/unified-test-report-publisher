package com.example.normalizer.uploader;

import com.example.normalizer.config.DashboardConfig;
import com.example.normalizer.model.NormalizedReportBundle;
import com.example.normalizer.telemetry.TelemetryLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UploadClient {

    private final DashboardConfig config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public UploadClient(DashboardConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();
    }

    public boolean upload(NormalizedReportBundle bundle) {
        if (config.getEndpoint() == null || config.getEndpoint().isEmpty()) {
            TelemetryLogger.logWarning("Upload endpoint is not configured. Skipping upload.");
            return true;
        }

        int maxRetries = config.getRetries();
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                String requestBody = objectMapper.writeValueAsString(bundle);

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(config.getEndpoint()))
                        .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                        .header("Content-Type", "application/json");

                if (config.getAuth() != null && "bearer".equalsIgnoreCase(config.getAuth().get("type"))) {
                    requestBuilder.header("Authorization", "Bearer " + config.getAuth().get("token"));
                }

                HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();

                long startTime = System.currentTimeMillis();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                long duration = System.currentTimeMillis() - startTime;

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    TelemetryLogger.logSuccess("Upload success: " + config.getEndpoint() + " (took " + duration + " ms)");
                    return true;
                } else {
                    TelemetryLogger.logError("Upload failed with status " + response.statusCode() + ": " + response.body());
                    if (attempt == maxRetries) return false;
                }
            } catch (IOException | InterruptedException e) {
                TelemetryLogger.logError("Upload attempt " + attempt + " failed: " + e.getMessage());
                if (attempt == maxRetries) return false;
            }

            try {
                Thread.sleep(2000L * attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
