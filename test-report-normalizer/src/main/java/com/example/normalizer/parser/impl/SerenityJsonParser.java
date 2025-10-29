package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.TestCase;
import com.example.normalizer.model.TestStep;
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

public class SerenityJsonParser implements ReportParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean canParse(File file) {
        return file.getName().toLowerCase().contains("serenity") && file.getName().endsWith(".json");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Serenity");

        JsonNode root = MAPPER.readTree(file);

        TestSuite suite = new TestSuite();
        suite.setName(root.path("name").asText("Serenity Suite"));

        List<TestCase> tests = new ArrayList<>();
        long suiteDuration = 0L;
        for (JsonNode testStepNode : root.path("testSteps")) {
            TestCase testCase = new TestCase();
            testCase.setName(testStepNode.path("description").asText("Unnamed Test"));
            testCase.setDurationMs(testStepNode.path("duration").asLong(0));
            testCase.setStatus(mapStatus(testStepNode.path("result").asText()));

            if (testStepNode.has("exception")) {
                testCase.setErrorMessage(testStepNode.path("exception").path("message").asText());
            }

            List<TestStep> steps = new ArrayList<>();
            for (JsonNode childStepNode : testStepNode.path("children")) {
                TestStep step = new TestStep();
                step.setName(childStepNode.path("description").asText("Unnamed Step"));
                step.setDurationMs(childStepNode.path("duration").asLong(0));
                step.setStatus(mapStatus(childStepNode.path("result").asText()));
                steps.add(step);
            }
            testCase.setSteps(steps);

            tests.add(testCase);
            suiteDuration += testCase.getDurationMs();
        }
        suite.setTests(tests);
        suite.setDurationMs(suiteDuration);

        report.setSuites(Collections.singletonList(suite));
        ReportSummaryCalculator.computeSummary(report);

        return report;
    }

    private String mapStatus(String result) {
        return switch (result.toUpperCase()) {
            case "SUCCESS" -> "PASSED";
            case "FAILURE" -> "FAILED";
            case "SKIPPED", "PENDING" -> "SKIPPED";
            default -> "UNKNOWN";
        };
    }

    @Override
    public com.example.normalizer.plugin.ParserPluginMetadata getMetadata() {
        com.example.normalizer.plugin.ParserPluginMetadata meta = new com.example.normalizer.plugin.ParserPluginMetadata();
        meta.setFramework("Serenity");
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(java.util.List.of(".json"));
        meta.setVersion("1.0");
        meta.setDescription("Parses Serenity BDD JSON report files.");
        return meta;
    }
}
