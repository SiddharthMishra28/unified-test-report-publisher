package com.example.normalizer.cli;

import com.example.normalizer.model.NormalizedReport;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.*;

public class NormalizedReportBundle {
    private String bundleId = UUID.randomUUID().toString();
    private String schemaVersion = "1.0";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt = Instant.now();

    private Map<String, String> metadata = new LinkedHashMap<>();
    private List<NormalizedReport> reports = new ArrayList<>();

    public String getBundleId() { return bundleId; }
    public String getSchemaVersion() { return schemaVersion; }
    public Instant getCreatedAt() { return createdAt; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public List<NormalizedReport> getReports() { return reports; }
    public void addReport(NormalizedReport r) { this.reports.add(r); }
}
