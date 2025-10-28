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
import java.util.Collections;
import java.util.List;

public class JUnitXmlParser implements ReportParser {

    private static final XmlMapper XML_MAPPER = new XmlMapper();

    @Override
    public boolean canParse(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".xml") && name.startsWith("test");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("JUnit");

        JsonNode suiteNode = XML_MAPPER.readTree(file);

        TestSuite suite = new TestSuite();
        suite.setName(suiteNode.path("name").asText("Unnamed Suite"));
        double totalTimeSeconds = suiteNode.path("time").asDouble(0.0);
        suite.setDurationMs((long) (totalTimeSeconds * 1000));

        List<TestCase> tests = new ArrayList<>();
        for (JsonNode testCaseNode : suiteNode.withArray("testcase")) {
            TestCase testCase = new TestCase();
            testCase.setName(testCaseNode.path("name").asText("Unnamed Test"));
            testCase.setClassName(testCaseNode.path("classname").asText(null));
            double durationSeconds = testCaseNode.path("time").asDouble(0.0);
            testCase.setDurationMs((long) (durationSeconds * 1000));

            if (testCaseNode.has("failure")) {
                testCase.setStatus("FAILED");
                testCase.setErrorMessage(testCaseNode.path("failure").asText());
            } else if (testCaseNode.has("skipped")) {
                testCase.setStatus("SKIPPED");
            } else {
                testCase.setStatus("PASSED");
            }
            tests.add(testCase);
        }
        suite.setTests(tests);
        report.setSuites(Collections.singletonList(suite));
        ReportSummaryCalculator.computeSummary(report);

        return report;
    }
}
