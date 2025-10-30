package com.example.normalizer.model;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class NormalizedReportBundle {
    private String schemaVersion = "1.0.0";
    private String bundleId = UUID.randomUUID().toString();
    private Instant createdAt = Instant.now();
    private Map<String, Object> metadata;
    private List<NormalizedReport> reports = new ArrayList<>();

    public void addReport(NormalizedReport report) {
        this.reports.add(report);
    }
}
