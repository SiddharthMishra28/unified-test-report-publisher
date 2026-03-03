package com.example.normalizer.config;

import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {

    @Test
    public void testConfigLoading() throws Exception {
        // Set up environment variables for substitution
        System.setProperty("CI_PIPELINE_ID", "12345");
        System.setProperty("GIT_COMMIT_SHA", "abcdef123");
        System.setProperty("UPLOAD_TOKEN", "my-secret-token");

        File configFile = new File("src/test/resources/normalizer-config.yaml");
        NormalizerConfig config = ConfigLoader.load(configFile);

        assertEquals("./reports", config.getInputDir());
        assertEquals("./output/normalized.json", config.getOutputFile());
        assertEquals("https://qa-dashboard.company.com/api/ingest", config.getDashboard().getEndpoint());
        assertEquals("BEARER", config.getDashboard().getAuth().get("type").toUpperCase());
        assertEquals("my-secret-token", config.getDashboard().getAuth().get("token"));
        assertEquals("12345", config.getMetadata().get("buildId"));
        assertEquals("abcdef123", config.getMetadata().get("gitCommit"));
        assertEquals("staging", config.getMetadata().get("environment"));
        assertEquals("User Platform", config.getMetadata().get("project"));
    }
}
