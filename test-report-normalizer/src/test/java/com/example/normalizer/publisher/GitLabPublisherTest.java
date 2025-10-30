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
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class GitLabPublisherTest {

    private HttpServer server;
    private TestHandler handler;
    private String serverUrl;
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        handler = new TestHandler();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        serverUrl = "http://localhost:" + server.getAddress().getPort();
        server.createContext("/api/v4/projects/12345/repository/files/", handler);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        tempFile = File.createTempFile("test-report", ".json");
        Files.writeString(tempFile.toPath(), "{\"data\": \"test\"}");
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
        tempFile.delete();
    }

    private GitLabConfig createTestConfig() {
        GitLabConfig config = new GitLabConfig();
        config.setGitlabProjectId("12345");
        config.setGitlabPersonalAccessToken("test-token");
        config.setRepoBranch("main");
        config.setGitlabUploadFolderPath("data/{{date}}/{{application}}/run_{{runHashBundleId}}.json");
        return config;
    }

    @Test
    public void testPublishReport_CreateNewFile() {
        GitLabConfig config = createTestConfig();
        GitLabPublisher publisher = new GitLabPublisher(config, serverUrl, true);

        handler.setFileExists(false);

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertTrue(success);
        assertEquals("POST", handler.getRequestMethod());
        assertTrue(handler.getRequestBody().contains("Create report"));
    }

    @Test
    public void testPublishReport_UpdateExistingFile() {
        GitLabConfig config = createTestConfig();
        GitLabPublisher publisher = new GitLabPublisher(config, serverUrl, true);

        handler.setFileExists(true);

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertTrue(success);
        assertEquals("PUT", handler.getRequestMethod());
        assertTrue(handler.getRequestBody().contains("Update report"));
    }

    @Test
    public void testPublishReport_RetryOnFailure() {
        GitLabConfig config = createTestConfig();
        GitLabPublisher publisher = new GitLabPublisher(config, serverUrl, true);

        handler.setFileExists(false);
        handler.setSuccessfulAttempt(3); // 1 GET (fail) + 1 POST (fail) + 1 POST (success)

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertTrue(success);
        assertEquals(3, handler.getRequestCount());
    }

    @Test
    public void testPublishReport_FailAfterRetries() {
        GitLabConfig config = createTestConfig();
        GitLabPublisher publisher = new GitLabPublisher(config, serverUrl, true);

        handler.setFileExists(false);
        handler.setSuccessfulAttempt(4); // All 3 attempts fail

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertFalse(success);
        assertEquals(3, handler.getRequestCount());
    }

    @Test
    public void testPublishReport_SkipIfNotInCi() {
        GitLabConfig config = createTestConfig();
        GitLabPublisher publisher = new GitLabPublisher(config, serverUrl, false); // Not in CI

        boolean success = publisher.publishReport(tempFile.getAbsolutePath(), "my-app");

        assertFalse(success);
        assertEquals(0, handler.getRequestCount());
    }

    private static class TestHandler implements HttpHandler {
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicReference<String> requestMethod = new AtomicReference<>();
        private final AtomicReference<String> requestBody = new AtomicReference<>();
        private boolean fileExists = false;
        private int successfulAttempt = 1;

        public void setFileExists(boolean fileExists) {
            this.fileExists = fileExists;
        }

        public void setSuccessfulAttempt(int successfulAttempt) {
            this.successfulAttempt = successfulAttempt;
        }

        public int getRequestCount() {
            return requestCount.get();
        }

        public String getRequestMethod() {
            return requestMethod.get();
        }

        public String getRequestBody() {
            return requestBody.get();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            int currentAttempt = requestCount.incrementAndGet();

            if ("GET".equals(exchange.getRequestMethod())) {
                if (fileExists) {
                    exchange.sendResponseHeaders(200, -1);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            } else { // POST or PUT
                requestMethod.set(exchange.getRequestMethod());
                try (InputStream is = exchange.getRequestBody()) {
                    requestBody.set(new String(is.readAllBytes(), StandardCharsets.UTF_8));
                }

                if (currentAttempt < successfulAttempt) {
                    exchange.sendResponseHeaders(500, -1);
                } else {
                    exchange.sendResponseHeaders("PUT".equals(exchange.getRequestMethod()) ? 200 : 201, -1);
                }
            }
            exchange.close();
        }
    }
}
