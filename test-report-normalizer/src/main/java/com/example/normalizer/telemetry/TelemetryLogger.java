package com.example.normalizer.telemetry;

import java.time.Instant;

public class TelemetryLogger {

    public static void log(String message) {
        System.out.printf("[%s] %s%n", Instant.now(), message);
    }

    public static void logSuccess(String endpoint, long durationMs) {
        System.out.printf("[%s] ✅ Upload success: %s (took %d ms)%n", Instant.now(), endpoint, durationMs);
    }

    public static void logFailure(String endpoint, int status, String body) {
        System.out.printf("[%s] ❌ Upload failed: %s [HTTP %d] - %s%n",
                Instant.now(), endpoint, status, body);
    }
}
