package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.TestCase;
import com.example.normalizer.model.TestSuite;
import com.example.normalizer.parser.ReportParser;
import com.example.normalizer.util.ReportSummaryCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PytestJsonParser implements ReportParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean canParse(File file) {
        return file.getName().toLowerCase().contains("pytest") && file.getName().endsWith(".json");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Pytest");

        JsonNode root = MAPPER.readTree(file);

        TestSuite suite = new TestSuite();
        suite.setName(root.path("root").asText("Pytest Suite"));
        suite.setDurationMs((long) (root.path("duration").asDouble(0.0) * 1000));

        List<TestCase> tests = new ArrayList<>();
        for (JsonNode testNode : root.path("tests")) {
            TestCase testCase = new TestCase();
            testCase.setName(testNode.path("nodeid").asText("Unnamed Test"));

            double setupDuration = testNode.path("setup").path("duration").asDouble(0.0);
            double callDuration = testNode.path("call").path("duration").asDouble(0.0);
            double teardownDuration = testNode.path("teardown").path("duration").asDouble(0.0);
            testCase.setDurationMs((long) ((setupDuration + callDuration + teardownDuration) * 1000));

            testCase.setStatus(mapStatus(testNode.path("outcome").asText()));
            if ("failed".equals(testNode.path("outcome").asText())) {
                testCase.setErrorMessage(testNode.path("call").path("longrepr").asText());
            }
            tests.add(testCase);
        }
        suite.setTests(tests);
        report.setSuites(Collections.singletonList(suite));
        ReportSummaryCalculator.computeSummary(report);

        return report;
    }

    private String mapStatus(String outcome) {
        return switch (outcome) {
            case "passed" -> "PASSED";
            case "failed" -> "FAILED";
            case "skipped" -> "SKIPPED";
            default -> "UNKNOWN";
        };
    }

    @Override
    public com.example.normalizer.plugin.ParserPluginMetadata getMetadata() {
        com.example.normalizer.plugin.ParserPluginMetadata meta = new com.example.normalizer.plugin.ParserPluginMetadata();
        meta.setFramework("Pytest");
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(java.util.List.of(".json"));
        meta.setVersion("1.0");
        meta.setDescription("Parses Pytest JSON report files (pytest-json-report format).");
        return meta;
    }
}
