package com.example.normalizer;

import com.example.normalizer.config.*;
import com.example.normalizer.model.NormalizedReportBundle;
import com.example.normalizer.publisher.GitLabPublisher;
import com.example.normalizer.telemetry.TelemetryLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Publisher {

    public static boolean
    publishReport(NormalizerConfig config, NormalizedReportBundle reportBundle) {
        try {
            // If publishOnlyOnCI is true but not in CI env — skip.
            if (config.isPublishOnlyOnCI() && System.getenv("CI_PIPELINE_ID") == null) {
                TelemetryLogger.publisher("Skipping publish: publishOnlyOnCI=true but not in CI pipeline.");
                return false;
            }

            if (config.getGitlab() == null) {
                TelemetryLogger.publisher("No GitLab configuration found — skipping publish.");
                return false;
            }

            // Create a temporary file to hold the report for publishing
            Path tempReportFile = Files.createTempFile("normalized-report-", ".json");
            ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            Files.writeString(tempReportFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportBundle));


            GitLabPublisher publisher = new GitLabPublisher(config.getGitlab());
            boolean success = publisher.publishReport(tempReportFile.toString(), config.getApplicationName());

            Files.delete(tempReportFile); // Clean up the temporary file

            if (success) {
                TelemetryLogger.logSuccess("Report successfully published to GitLab repository.");
                writeAuditLog(config);
            } else {
                TelemetryLogger.logError("Failed to publish report to GitLab.");
            }
            return success;

        } catch (Exception e) {
            TelemetryLogger.logError("Exception in publishReport(): " + e.getMessage());
            return false;
        }
    }

    private static void writeAuditLog(NormalizerConfig config) {
        String auditFile = "publish_audit.log";
        try (FileWriter fw = new FileWriter(auditFile, true)) {
            fw.write(String.format("[%s] Published %s to GitLab project %s on branch %s%n",
                    LocalDateTime.now(),
                    config.getOutputFile(),
                    config.getGitlab().getGitlabProjectId(),
                    config.getGitlab().getRepoBranch()));
        } catch (IOException e) {
            TelemetryLogger.logWarning("Failed to write audit log: " + e.getMessage());
        }
    }
}
