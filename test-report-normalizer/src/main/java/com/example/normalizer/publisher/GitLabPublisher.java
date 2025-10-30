package com.example.normalizer.publisher;

import com.example.normalizer.config.GitLabConfig;
import com.example.normalizer.telemetry.TelemetryLogger;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Base64;
import java.util.UUID;

public class GitLabPublisher {

    private final GitLabConfig config;
    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private String gitlabUrl = "https://gitlab.com"; // Default URL

    public GitLabPublisher(GitLabConfig config) {
        this.config = config;
    }

    // Package-private for testing
    void setGitlabUrl(String gitlabUrl) {
        this.gitlabUrl = gitlabUrl;
    }

    public boolean publishReport(String filePath, String applicationName) {
        try {
            if (System.getProperty("CI_PIPELINE_ID") == null && System.getenv("CI_PIPELINE_ID") == null) {
                TelemetryLogger.log("Skipping publish: Not running in CI environment.");
                return false;
            }

            String projectId = config.getGitlabProjectId();
            String token = config.getGitlabPersonalAccessToken();

            // Resolve the templated path
            String runId = UUID.randomUUID().toString().substring(0, 8);
            String date = LocalDate.now().toString();
            String targetPath = config.getGitlabUploadFolderPath()
                    .replace("{{date}}", date)
                    .replace("{{application}}", applicationName)
                    .replace("{{runHashBundleId}}", runId);

            String encodedPath = URLEncoder.encode(targetPath, StandardCharsets.UTF_8);

            String fileContent = Files.readString(java.nio.file.Path.of(filePath));
            String base64Content = Base64.getEncoder().encodeToString(fileContent.getBytes(StandardCharsets.UTF_8));

            // Construct GitLab API endpoint
            String url = String.format(
                "%s/api/v4/projects/%s/repository/files/%s",
                gitlabUrl,
                URLEncoder.encode(projectId, StandardCharsets.UTF_8),
                encodedPath
            );

            // Create file upload JSON payload
            String payload = String.format("""
                {
                  "branch": "%s",
                  "content": "%s",
                  "commit_message": "Publish normalized test report %s"
                }
                """, config.getRepoBranch(), base64Content, runId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("PRIVATE-TOKEN", token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201) {
                TelemetryLogger.logSuccess("GitLab publish", 0);
                TelemetryLogger.log("✅ Successfully published report to GitLab at: " + targetPath);
                return true;
            } else if (response.statusCode() == 400 && response.body().contains("already exists")) {
                TelemetryLogger.log("⚠️ File already exists — skipping overwrite.");
                return true;
            } else {
                TelemetryLogger.logFailure("GitLab publish", response.statusCode(), response.body());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            TelemetryLogger.log("❌ Exception during GitLab publishing: " + e.getMessage());
            return false;
        }
    }
}
