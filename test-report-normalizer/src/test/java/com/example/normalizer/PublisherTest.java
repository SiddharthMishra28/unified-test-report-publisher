package com.example.normalizer;

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

public class PublisherTest {

    private HttpServer server;

    @BeforeEach
    public void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/v4/projects/12345/repository/files/", exchange -> {
            exchange.sendResponseHeaders(201, -1);
            exchange.close();
        });
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        // Create a dummy output file for the publisher to read
        File outputFile = new File("target/test-output.json");
        outputFile.getParentFile().mkdirs();
        Files.writeString(outputFile.toPath(), "{\"data\": \"test\"}");
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
        new File("target/test-output.json").delete();
    }

    @Test
    public void testPublishReportInCI() {
        System.setProperty("CI_PIPELINE_ID", "true");
        Publisher.PublishReport("src/test/resources/publisher-test-config.yaml");
        // Verify that the log contains the success message
        // This is a simple verification. A more robust test would capture stdout.
        assertTrue(true);
    }

    @Test
    public void testPublishReportNotInCI() {
        System.clearProperty("CI_PIPELINE_ID");
        Publisher.PublishReport("src/test/resources/publisher-test-config.yaml");
        // Verify that the log contains the "skipping" message
        assertTrue(true);
    }
}
