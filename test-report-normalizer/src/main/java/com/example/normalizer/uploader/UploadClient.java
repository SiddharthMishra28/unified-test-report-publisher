package com.example.normalizer.uploader;

import com.example.normalizer.telemetry.TelemetryLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public class UploadClient {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final UploadConfig config;

    public UploadClient(UploadConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    public boolean upload(Object bundle) {
        try {
            String body = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(config.getEndpoint()))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json");

            // Apply authentication headers
            if (config.getAuthType() != null) {
                switch (config.getAuthType()) {
                    case BASIC -> {
                        String encoded = Base64.getEncoder().encodeToString(
                                (config.getUsername() + ":" + config.getPassword()).getBytes()
                        );
                        builder.header("Authorization", "Basic " + encoded);
                    }
                    case BEARER -> builder.header("Authorization", "Bearer " + config.getToken());
                    case API_KEY -> builder.header("x-api-key", config.getToken());
                    case CUSTOM_HEADER -> {
                        for (Map.Entry<String, String> e : config.getCustomHeaders().entrySet()) {
                            builder.header(e.getKey(), e.getValue());
                        }
                    }
                    default -> {} // No auth
                }
            }

            HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();

            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                long start = System.currentTimeMillis();
                try {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    long duration = System.currentTimeMillis() - start;
                    int code = response.statusCode();

                    if (code >= 200 && code < 300) {
                        TelemetryLogger.logSuccess(config.getEndpoint(), duration);
                        return true;
                    } else {
                        TelemetryLogger.logFailure(config.getEndpoint(), code, response.body());
                    }
                } catch (IOException | InterruptedException e) {
                    TelemetryLogger.log("Attempt " + attempt + " failed: " + e.getMessage());
                }

                if (attempt < MAX_RETRIES) {
                    TelemetryLogger.log("Retrying (" + attempt + "/" + MAX_RETRIES + ") after 3s...");
                    Thread.sleep(3000);
                }
            }
            throw new IOException("Upload failed after " + MAX_RETRIES + " retries");
        } catch (Exception e) {
            TelemetryLogger.log("Fatal error during upload: " + e.getMessage());
            return false;
        }
    }
}
