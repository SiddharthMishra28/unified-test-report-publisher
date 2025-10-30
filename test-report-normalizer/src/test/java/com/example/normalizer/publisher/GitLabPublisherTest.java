package com.example.normalizer.publisher;

import com.example.normalizer.config.GitLabConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitLabPublisherTest {

    private HttpServer server;
    private TestHandler handler;
    private String serverUrl;

    @BeforeEach
    public void setUp() throws IOException {
        handler = new TestHandler();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        serverUrl = "http://localhost:" + server.getAddress().getPort();
        server.createContext("/api/v4/projects/12345/repository/files/", handler);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        System.setProperty("CI_PIPELINE_ID", "true");
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
        System.clearProperty("CI_PIPELINE_ID");
    }

    @Test
    public void testPublishReportSuccess() throws IOException {
        GitLabConfig config = new GitLabConfig();
        config.setGitlabProjectId("12345");
        config.setGitlabPersonalAccessToken("test-token");
        config.setRepoBranch("main");
        config.setGitlabUploadFolderPath("data/{{date}}/{{application}}/run_{{runHashBundleId}}.json");

        File tempFile = File.createTempFile("test-report", ".json");
        Files.writeString(tempFile.toPath(), "{\"data\": \"test\"}");

        GitLabPublisher publisher = new GitLabPublisher(config);

        // Override the URL for testing
        publisher.setGitlabUrl(serverUrl);

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertTrue(success);
        assertEquals("test-token", handler.privateToken);
        tempFile.delete();
    }

    @Test
    public void testPublishReportFailure() throws IOException {
        handler.setResponseCode(500);
        GitLabConfig config = new GitLabConfig();
        config.setGitlabProjectId("12345");
        config.setGitlabPersonalAccessToken("test-token");
        config.setRepoBranch("main");
        config.setGitlabUploadFolderPath("data/{{date}}/{{application}}/run_{{runHashBundleId}}.json");

        File tempFile = File.createTempFile("test-report", ".json");
        Files.writeString(tempFile.toPath(), "{\"data\": \"test\"}");

        GitLabPublisher publisher = new GitLabPublisher(config);

        // Override the URL for testing
        publisher.setGitlabUrl(serverUrl);

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertFalse(success);
        tempFile.delete();
    }

    private static class TestHandler implements HttpHandler {
        private int responseCode = 201;
        public String privateToken;

        public void setResponseCode(int responseCode) {
            this.responseCode = responseCode;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            privateToken = exchange.getRequestHeaders().getFirst("PRIVATE-TOKEN");
            exchange.sendResponseHeaders(responseCode, -1);
            exchange.close();
        }
    }
}
