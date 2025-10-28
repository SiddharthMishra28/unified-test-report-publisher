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
import java.util.List;

public class CucumberJsonParser implements ReportParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public boolean canParse(File file) {
        return file.getName().toLowerCase().contains("cucumber") && file.getName().endsWith(".json");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Cucumber");
        List<TestSuite> suites = new ArrayList<>();

        JsonNode root = MAPPER.readTree(file);

        for (JsonNode featureNode : root) {
            TestSuite suite = new TestSuite();
            suite.setName(featureNode.path("name").asText("Unnamed Feature"));
            List<TestCase> tests = new ArrayList<>();
            long suiteDuration = 0L;

            for (JsonNode scenarioNode : featureNode.path("elements")) {
                if (!scenarioNode.path("type").asText("").equals("scenario")) continue;

                TestCase testCase = new TestCase();
                testCase.setName(scenarioNode.path("name").asText("Unnamed Scenario"));
                List<TestStep> steps = new ArrayList<>();
                long scenarioDuration = 0L;
                String scenarioStatus = "PASSED";

                for (JsonNode stepNode : scenarioNode.path("steps")) {
                    TestStep step = new TestStep();
                    step.setName(stepNode.path("name").asText("Unnamed Step"));
                    String status = stepNode.path("result").path("status").asText("unknown").toUpperCase();
                    long durationNanos = stepNode.path("result").path("duration").asLong(0);
                    long durationMillis = durationNanos / 1_000_000;

                    step.setStatus(status);
                    step.setDurationMs(durationMillis);
                    steps.add(step);
                    scenarioDuration += durationMillis;

                    if ("FAILED".equals(status)) {
                        scenarioStatus = "FAILED";
                    } else if ("SKIPPED".equals(status) && !"FAILED".equals(scenarioStatus)) {
                        scenarioStatus = "SKIPPED";
                    }
                }
                testCase.setSteps(steps);
                testCase.setDurationMs(scenarioDuration);
                testCase.setStatus(scenarioStatus);
                tests.add(testCase);
                suiteDuration += scenarioDuration;
            }
            suite.setTests(tests);
            suite.setDurationMs(suiteDuration);
            suites.add(suite);
        }
        report.setSuites(suites);
        ReportSummaryCalculator.computeSummary(report);

        return report;
    }
}
