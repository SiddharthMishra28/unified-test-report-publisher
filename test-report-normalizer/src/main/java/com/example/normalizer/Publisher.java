package com.example.normalizer;

import com.example.normalizer.config.*;
import com.example.normalizer.publisher.GitLabPublisher;
import com.example.normalizer.telemetry.TelemetryLogger;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

public class Publisher {

    public static void PublishReport(String configPath) {
        try {
            NormalizerConfig config = ConfigLoader.load(new File(configPath));

            // If publishOnlyOnCI is true but not in CI env — skip.
            if (config.isPublishOnlyOnCI() && System.getenv("CI_PIPELINE_ID") == null) {
                TelemetryLogger.log("🚫 Skipping publish: publishOnlyOnCI=true but not in CI pipeline.");
                return;
            }

            if (config.getGitlab() == null) {
                TelemetryLogger.log("⚠️ No GitLab configuration found — skipping publish.");
                return;
            }

            String outputFilePath = config.getOutputFile();
            if (outputFilePath == null || !new File(outputFilePath).exists()) {
                TelemetryLogger.log("❌ Output file not found for publishing: " + outputFilePath);
                return;
            }

            GitLabPublisher publisher = new GitLabPublisher(config.getGitlab());
            boolean success = publisher.publishReport(outputFilePath, config.getApplicationName());

            if (success) {
                TelemetryLogger.log("🎉 Report successfully published to GitLab repository.");

                // Append audit log
                String auditFile = "publish_audit.log";
                try (FileWriter fw = new FileWriter(auditFile, true)) {
                    fw.write(String.format("[%s] Published %s to GitLab project %s on branch %s%n",
                            LocalDateTime.now(),
                            config.getOutputFile(),
                            config.getGitlab().getGitlabProjectId(),
                            config.getGitlab().getRepoBranch()));
                } catch (Exception e) {
                    TelemetryLogger.log("⚠️ Failed to write audit log: " + e.getMessage());
                }

            } else {
                TelemetryLogger.log("❌ Failed to publish report to GitLab.");
            }

        } catch (Exception e) {
            TelemetryLogger.log("❌ Exception in PublishReport(): " + e.getMessage());
        }
    }
}
