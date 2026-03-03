package com.example.normalizer.uploader;

import com.example.normalizer.config.DashboardConfig;
import com.example.normalizer.model.NormalizedReportBundle;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UploadClientTest {

    private HttpServer server;
    private TestHandler handler;

    @BeforeEach
    public void setUp() throws IOException {
        handler = new TestHandler();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/ingest", handler);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void testUploadWithBearerToken() {
        DashboardConfig config = new DashboardConfig();
        config.setEndpoint("http://localhost:" + server.getAddress().getPort() + "/api/ingest");
        config.setAuth(Map.of("type", "bearer", "token", "my-token"));


        UploadClient client = new UploadClient(config);
        NormalizedReportBundle bundle = new NormalizedReportBundle();

        boolean success = client.upload(bundle);
        assertTrue(success);
        assertEquals("Bearer my-token", handler.authHeader);
    }

    @Test
    public void testUploadSuccess() {
        DashboardConfig config = new DashboardConfig();
        config.setEndpoint("http://localhost:" + server.getAddress().getPort() + "/api/ingest");

        UploadClient client = new UploadClient(config);
        NormalizedReportBundle bundle = new NormalizedReportBundle();

        boolean success = client.upload(bundle);
        assertTrue(success);
    }

    private static class TestHandler implements HttpHandler {
        public String authHeader;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        }
    }
}
