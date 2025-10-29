package com.example.normalizer.uploader;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

public class UploadClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int MAX_RETRIES = 3;
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    public static boolean upload(Object bundle, UploadConfig config) {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();

        try {
            String body = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(bundle);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(config.getEndpoint()))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json");

            // Apply authentication headers
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

            HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(body)).build();

            // Retry with exponential backoff
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                try {
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    int code = response.statusCode();

                    System.out.println("[UploadClient] Attempt " + attempt + " - Response Code: " + code);
                    if (code >= 200 && code < 300) {
                        System.out.println("[UploadClient] Upload successful.");
                        return true;
                    } else {
                        System.err.println("[UploadClient] Server returned " + code + ": " + response.body());
                    }
                } catch (IOException | InterruptedException e) {
                    System.err.println("[UploadClient] Attempt " + attempt + " failed: " + e.getMessage());
                }

                try {
                    Thread.sleep(2000L * attempt); // Exponential backoff
                } catch (InterruptedException ignored) {}
            }

            System.err.println("[UploadClient] All retries failed.");
            return false;

        } catch (Exception e) {
            System.err.println("[UploadClient] Fatal error: " + e.getMessage());
            return false;
        }
    }
}
