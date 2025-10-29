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
import java.util.List;

public class MochaJsonParser implements ReportParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean canParse(File file) {
        return file.getName().toLowerCase().contains("mocha") && file.getName().endsWith(".json");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Mocha");

        JsonNode root = MAPPER.readTree(file);
        List<TestSuite> suites = new ArrayList<>();

        for (JsonNode suiteNode : root.path("results")) {
            parseSuite(suiteNode, suites);
        }

        report.setSuites(suites);
        ReportSummaryCalculator.computeSummary(report);

        return report;
    }

    private void parseSuite(JsonNode suiteNode, List<TestSuite> suites) {
        TestSuite suite = new TestSuite();
        suite.setName(suiteNode.path("title").asText("Unnamed Suite"));
        suite.setDurationMs(suiteNode.path("duration").asLong(0));

        List<TestCase> tests = new ArrayList<>();
        for (JsonNode testNode : suiteNode.path("tests")) {
            TestCase testCase = new TestCase();
            testCase.setName(testNode.path("title").asText("Unnamed Test"));
            testCase.setDurationMs(testNode.path("duration").asLong(0));
            testCase.setStatus(mapStatus(testNode.path("state").asText()));
            if (testNode.has("err") && testNode.get("err").has("message")) {
                testCase.setErrorMessage(testNode.get("err").get("message").asText());
            }
            tests.add(testCase);
        }
        suite.setTests(tests);
        suites.add(suite);

        // Recursively parse nested suites
        for (JsonNode nestedSuiteNode : suiteNode.path("suites")) {
            parseSuite(nestedSuiteNode, suites);
        }
    }

    private String mapStatus(String state) {
        return switch (state) {
            case "passed" -> "PASSED";
            case "failed" -> "FAILED";
            default -> "SKIPPED";
        };
    }

    @Override
    public com.example.normalizer.plugin.ParserPluginMetadata getMetadata() {
        com.example.normalizer.plugin.ParserPluginMetadata meta = new com.example.normalizer.plugin.ParserPluginMetadata();
        meta.setFramework("Mocha");
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(java.util.List.of(".json"));
        meta.setVersion("1.0");
        meta.setDescription("Parses Mocha JSON report files (mochawesome format).");
        return meta;
    }
}
