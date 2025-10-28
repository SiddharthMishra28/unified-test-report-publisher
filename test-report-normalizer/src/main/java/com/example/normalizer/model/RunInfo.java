package com.example.normalizer.model;

import lombok.Data;

@Data
public class RunInfo {
    private String buildId;            // CI/CD build or pipeline ID
    private String environment;        // e.g. "staging", "prod"
    private String triggeredBy;        // user or system
    private String startTime;          // ISO format: 2025-10-28T10:00:00Z
    private String endTime;            // ISO format
    private Long durationMs;           // Total duration in milliseconds
}
