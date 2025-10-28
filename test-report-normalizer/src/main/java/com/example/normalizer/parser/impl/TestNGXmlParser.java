package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.TestCase;
import com.example.normalizer.model.TestSuite;
import com.example.normalizer.parser.ReportParser;
import com.example.normalizer.util.ReportSummaryCalculator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestNGXmlParser implements ReportParser {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    @Override
    public boolean canParse(File file) {
        String name = file.getName().toLowerCase();
        return name.contains("testng-results") && name.endsWith(".xml");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("TestNG");

        JsonNode root = XML_MAPPER.readTree(file);
        List<TestSuite> suites = new ArrayList<>();

        for (JsonNode suiteNode : getNodesAsList(root, "suite")) {
            suites.add(parseSuiteNode(suiteNode));
        }

        report.setSuites(suites);
        ReportSummaryCalculator.computeSummary(report);

        return report;
    }

    private TestSuite parseSuiteNode(JsonNode suiteNode) {
        TestSuite suite = new TestSuite();
        suite.setName(suiteNode.path("name").asText("Unnamed Suite"));
        suite.setDurationMs(suiteNode.path("duration-ms").asLong(0));

        List<TestCase> tests = new ArrayList<>();
        for (JsonNode testNode : getNodesAsList(suiteNode, "test")) {
            for (JsonNode classNode : getNodesAsList(testNode, "class")) {
                for (JsonNode methodNode : getNodesAsList(classNode, "test-method")) {
                    TestCase testCase = new TestCase();
                    testCase.setName(methodNode.path("name").asText("Unnamed Test"));
                    testCase.setClassName(classNode.path("name").asText(null));
                    testCase.setDurationMs(methodNode.path("duration-ms").asLong(0));

                    String status = methodNode.path("status").asText("UNKNOWN");
                    testCase.setStatus(mapStatus(status));

                    if (methodNode.has("exception")) {
                        testCase.setErrorMessage(methodNode.path("exception").path("message").asText());
                    }
                    tests.add(testCase);
                }
            }
        }
        suite.setTests(tests);
        return suite;
    }

    private List<JsonNode> getNodesAsList(JsonNode parentNode, String fieldName) {
        List<JsonNode> nodes = new ArrayList<>();
        JsonNode fieldNode = parentNode.path(fieldName);
        if (fieldNode.isArray()) {
            fieldNode.forEach(nodes::add);
        } else if (!fieldNode.isMissingNode()) {
            nodes.add(fieldNode);
        }
        return nodes;
    }

    private String mapStatus(String raw) {
        return switch (raw.toUpperCase()) {
            case "PASS" -> "PASSED";
            case "FAIL" -> "FAILED";
            case "SKIP" -> "SKIPPED";
            default -> "UNKNOWN";
        };
    }
}
