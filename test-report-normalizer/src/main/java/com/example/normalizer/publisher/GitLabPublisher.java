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
    private final String gitlabUrl;
    private final boolean isCiEnvironment;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(30))
            .build();

    public GitLabPublisher(GitLabConfig config) {
        this(config, "https://gitlab.com", System.getenv("CI_PIPELINE_ID") != null);
    }

    GitLabPublisher(GitLabConfig config, String gitlabUrl, boolean isCiEnvironment) {
        this.config = config;
        this.gitlabUrl = gitlabUrl;
        this.isCiEnvironment = isCiEnvironment;
    }

    public boolean publishReport(String filePath, String applicationName) {
        try {
            if (!isCiEnvironment) {
                TelemetryLogger.publisher("Skipping publish: Not running in CI environment.");
                return false;
            }

            TelemetryLogger.publisher("Starting GitLab upload for application: " + applicationName);

            String projectId = config.getGitlabProjectId();
            String token = config.getGitlabPersonalAccessToken();

            String runId = UUID.randomUUID().toString().substring(0, 8);
            String date = LocalDate.now().toString();

            String targetPath = config.getGitlabUploadFolderPath()
                    .replace("{{date}}", date)
                    .replace("{{application}}", applicationName)
                    .replace("{{runHashBundleId}}", runId);

            String encodedPath = URLEncoder.encode(targetPath, StandardCharsets.UTF_8);
            String encodedBranch = URLEncoder.encode(config.getRepoBranch(), StandardCharsets.UTF_8);
            String projectEncoded = URLEncoder.encode(projectId, StandardCharsets.UTF_8);

            String baseUrl = gitlabUrl + "/api/v4/projects/" + projectEncoded + "/repository/files/" + encodedPath;

            String fileContent = Files.readString(java.nio.file.Path.of(filePath));
            String base64Content = Base64.getEncoder().encodeToString(fileContent.getBytes(StandardCharsets.UTF_8));

            boolean fileExists = checkFileExists(baseUrl, token, encodedBranch);
            TelemetryLogger.publisher("File exists check complete. Exists: " + fileExists);


            String payload = String.format("""
                {
                  "branch": "%s",
                  "content": "%s",
                  "commit_message": "%s %s"
                }
                """,
                    config.getRepoBranch(),
                    base64Content,
                    fileExists ? "Update report" : "Create report",
                    targetPath);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl))
                    .header("PRIVATE-TOKEN", token)
                    .header("Content-Type", "application/json")
                    .method(fileExists ? "PUT" : "POST", HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = sendWithRetry(request, 2);

            if (response != null && (response.statusCode() == 201 || response.statusCode() == 200)) {
                TelemetryLogger.logSuccess("Successfully " + (fileExists ? "updated" : "created") + " report: " + targetPath);
                return true;
            } else {
                if (response != null) {
                    TelemetryLogger.logError("GitLab publish failed with status " + response.statusCode() + ": " + response.body());
                } else {
                    TelemetryLogger.logError("GitLab publish failed after multiple retries.");
                }
                return false;
            }
        } catch (IOException | InterruptedException e) {
            TelemetryLogger.logError("Exception during GitLab publishing: " + e.getMessage());
            return false;
        }
    }

    private boolean checkFileExists(String baseUrl, String token, String encodedBranch) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "?ref=" + encodedBranch))
                    .header("PRIVATE-TOKEN", token)
                    .GET()
                    .build();

            HttpResponse<String> response = sendWithRetry(request, 2);
            return response != null && response.statusCode() == 200;
        } catch (Exception e) {
            TelemetryLogger.logWarning("Could not verify file existence: " + e.getMessage());
            return false;
        }
    }

    private HttpResponse<String> sendWithRetry(HttpRequest request, int maxRetries) throws IOException, InterruptedException {
        int attempts = 0;
        HttpResponse<String> response = null;
        while (attempts < maxRetries) {
            attempts++;
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 500) return response; // success or client error
            TelemetryLogger.logWarning("Retry " + attempts + ": GitLab returned " + response.statusCode());
            if (attempts < maxRetries) {
                Thread.sleep(2000L * attempts);
            }
        }
        return response;
    }
}
