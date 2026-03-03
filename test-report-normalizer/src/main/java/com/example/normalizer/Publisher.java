package com.example.normalizer;

import com.example.normalizer.config.NormalizerConfig;
import com.example.normalizer.model.NormalizedReportBundle;
import com.example.normalizer.publisher.GitLabPublisher;
import com.example.normalizer.telemetry.TelemetryLogger;
import com.example.normalizer.uploader.UploadClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Publisher {

    public static boolean publishReport(NormalizerConfig config, NormalizedReportBundle reportBundle) {
        try {
            if (config.isPublishOnlyOnCI() && System.getenv("CI_PIPELINE_ID") == null) {
                TelemetryLogger.publisher("Skipping publish: publishOnlyOnCI=true but not in CI pipeline.");
                return false;
            }

            boolean dashboardSuccess = publishToDashboard(config, reportBundle);
            boolean gitlabSuccess = publishToGitlab(config, reportBundle);

            if (dashboardSuccess || gitlabSuccess) {
                writeAuditLog(config, dashboardSuccess, gitlabSuccess);
            }

            return dashboardSuccess || gitlabSuccess;
        } catch (Exception e) {
            TelemetryLogger.logError("Exception in publishReport(): " + e.getMessage());
            return false;
        }
    }

    private static boolean publishToDashboard(NormalizerConfig config, NormalizedReportBundle reportBundle) {
        if (config.getDashboard() == null || config.getDashboard().getEndpoint() == null || config.getDashboard().getEndpoint().isBlank()) {
            TelemetryLogger.publisher("No dashboard configuration found — skipping dashboard upload.");
            return false;
        }

        UploadClient uploadClient = new UploadClient(config.getDashboard());
        boolean success = uploadClient.upload(reportBundle);
        if (success) {
            TelemetryLogger.logSuccess("Report successfully published to dashboard endpoint.");
        } else {
            TelemetryLogger.logError("Failed to publish report to dashboard endpoint.");
        }
        return success;
    }

    private static boolean publishToGitlab(NormalizerConfig config, NormalizedReportBundle reportBundle) throws IOException {
        if (config.getGitlab() == null) {
            TelemetryLogger.publisher("No GitLab configuration found — skipping GitLab publish.");
            return false;
        }

        Path tempReportFile = Files.createTempFile("normalized-report-", ".json");
        try {
            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            Files.writeString(tempReportFile, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(reportBundle));

            GitLabPublisher publisher = new GitLabPublisher(config.getGitlab());
            boolean success = publisher.publishReport(tempReportFile.toString(), config.getApplicationName());

            if (success) {
                TelemetryLogger.logSuccess("Report successfully published to GitLab repository.");
            } else {
                TelemetryLogger.logError("Failed to publish report to GitLab.");
            }
            return success;
        } finally {
            Files.deleteIfExists(tempReportFile);
        }
    }

    private static void writeAuditLog(NormalizerConfig config, boolean dashboardPublished, boolean gitlabPublished) {
        String auditFile = "publish_audit.log";
        try (FileWriter fw = new FileWriter(auditFile, true)) {
            fw.write(String.format("[%s] Published %s | dashboard=%s | gitlab=%s%n",
                    LocalDateTime.now(),
                    config.getOutputFile(),
                    dashboardPublished,
                    gitlabPublished));
        } catch (IOException e) {
            TelemetryLogger.logWarning("Failed to write audit log: " + e.getMessage());
        }
    }
}
