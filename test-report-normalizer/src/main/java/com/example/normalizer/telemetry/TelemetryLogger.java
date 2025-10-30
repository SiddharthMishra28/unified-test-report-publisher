package com.example.normalizer.telemetry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class TelemetryLogger {

    private static final boolean JSON_MODE =
            Boolean.parseBoolean(System.getenv().getOrDefault("NORMALIZER_JSON_LOGS", "false"));
    private static final boolean COLOR_MODE =
            !"false".equalsIgnoreCase(System.getenv().getOrDefault("NORMALIZER_COLOR_LOGS", "true"));

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String RESET  = "\u001B[0m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";

    public static void log(String message) {
        emit("INFO", message, BLUE);
    }

    public static void logSuccess(String message) {
        emit("SUCCESS", message, GREEN);
    }

    public static void logWarning(String message) {
        emit("WARN", message, YELLOW);
    }

    public static void logError(String message) {
        emit("ERROR", message, RED);
    }

    public static void parser(String msg)    { log("[PARSER] " + msg); }
    public static void normalizer(String msg){ log("[NORMALIZER] " + msg); }
    public static void publisher(String msg) { log("[PUBLISHER] " + msg); }


    private static void emit(String level, String message, String color) {
        String time = LocalDateTime.now().format(FORMATTER);

        if (JSON_MODE) {
            Map<String, Object> log = new HashMap<>();
            log.put("timestamp", time);
            log.put("level", level);
            log.put("message", message);
            System.out.println(toJson(log));
        } else {
            String output = String.format("[%s] [%s] %s",
                    time, level, message);
            System.out.println(COLOR_MODE ? color + output + RESET : output);
        }
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (var e : map.entrySet()) {
            if (!first) sb.append(", ");
            sb.append("\"").append(e.getKey()).append("\": ");
            sb.append("\"").append(e.getValue().toString().replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static void summary(String application, boolean published) {
        String banner = "\n========== TEST NORMALIZATION SUMMARY ==========\n"
                + "Application : " + application + "\n"
                + "Published   : " + (published ? "✅ YES" : "❌ NO") + "\n"
                + "Timestamp   : " + LocalDateTime.now().format(FORMATTER) + "\n"
                + "================================================\n";
        System.out.println(banner);
    }
}
