package com.example.normalizer.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SchemaValidatorTest {

    @Test
    public void testSchemaValidation() throws Exception {
        // Create a valid bundle object
        com.example.normalizer.cli.NormalizedReportBundle bundle = new com.example.normalizer.cli.NormalizedReportBundle();
        com.example.normalizer.model.NormalizedReport report = new com.example.normalizer.model.NormalizedReport();
        report.setFramework("JUnit");
        com.example.normalizer.model.TestSuite suite = new com.example.normalizer.model.TestSuite();
        suite.setName("Test Suite");
        suite.setDurationMs(123L);
        suite.setTests(java.util.Collections.emptyList());
        report.setSuites(java.util.Collections.singletonList(suite));
        bundle.addReport(report);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Convert the bundle to a JsonNode
        com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.valueToTree(bundle);

        // Validate the JsonNode
        boolean isValid = SchemaValidator.validate(jsonNode);
        assertTrue(isValid);
    }
}
