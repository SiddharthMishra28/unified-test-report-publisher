package com.example.normalizer.validation;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.NormalizedReportBundle;
import com.example.normalizer.model.TestSuite;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaValidatorTest {

    @Test
    public void testSchemaValidation() throws Exception {
        // Create a valid bundle object
        NormalizedReportBundle bundle = new NormalizedReportBundle();
        bundle.setMetadata(new java.util.HashMap<>());
        NormalizedReport report = new NormalizedReport();
        report.setFramework("JUnit");
        TestSuite suite = new TestSuite();
        suite.setName("Test Suite");
        suite.setDurationMs(123L);
        suite.setTests(java.util.Collections.emptyList());
        report.setSuites(java.util.Collections.singletonList(suite));
        bundle.addReport(report);

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Convert the bundle to a JsonNode
        com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.valueToTree(bundle);

        // Validate the JsonNode
        boolean isValid = SchemaValidator.validate(jsonNode);
        assertTrue(isValid);
    }
}
