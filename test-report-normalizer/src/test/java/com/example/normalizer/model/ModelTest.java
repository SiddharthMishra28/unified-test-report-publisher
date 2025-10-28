package com.example.normalizer.model;

import com.example.normalizer.util.ReportSummaryCalculator;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ModelTest {

    @Test
    public void testSummaryComputation() {
        TestCase t1 = new TestCase();
        t1.setName("Test A");
        t1.setStatus("PASSED");

        TestCase t2 = new TestCase();
        t2.setName("Test B");
        t2.setStatus("FAILED");

        TestSuite suite = new TestSuite();
        suite.setName("Suite 1");
        suite.setTests(List.of(t1, t2));

        NormalizedReport report = new NormalizedReport();
        report.setSuites(List.of(suite));

        Summary summary = ReportSummaryCalculator.computeSummary(report);
        assertEquals(2, summary.getTotal());
        assertEquals(1, summary.getPassed());
        assertEquals(1, summary.getFailed());
    }

    @Test
    public void testSummaryComputationWithNullReport() {
        Summary summary = ReportSummaryCalculator.computeSummary(null);
        assertNotNull(summary);
        assertEquals(0, summary.getTotal());
    }

    @Test
    public void testSummaryComputationWithNullSuites() {
        NormalizedReport report = new NormalizedReport();
        report.setSuites(null);
        Summary summary = ReportSummaryCalculator.computeSummary(report);
        assertNotNull(summary);
        assertEquals(0, summary.getTotal());
    }

    @Test
    public void testSummaryComputationWithNullTestsInSuite() {
        TestSuite suite = new TestSuite();
        suite.setName("Suite 1");
        suite.setTests(null);

        NormalizedReport report = new NormalizedReport();
        report.setSuites(List.of(suite));

        Summary summary = ReportSummaryCalculator.computeSummary(report);
        assertNotNull(summary);
        assertEquals(0, summary.getTotal());
    }

    @Test
    public void testSummaryComputationWithNullTestCase() {
        TestSuite suite = new TestSuite();
        suite.setName("Suite 1");
        suite.setTests(Collections.singletonList(null));

        NormalizedReport report = new NormalizedReport();
        report.setSuites(List.of(suite));

        Summary summary = ReportSummaryCalculator.computeSummary(report);
        assertNotNull(summary);
        assertEquals(0, summary.getTotal());
    }

    @Test
    public void testSummaryComputationWithNullStatus() {
        TestCase t1 = new TestCase();
        t1.setName("Test A");
        t1.setStatus(null);

        TestSuite suite = new TestSuite();
        suite.setName("Suite 1");
        suite.setTests(List.of(t1));

        NormalizedReport report = new NormalizedReport();
        report.setSuites(List.of(suite));

        Summary summary = ReportSummaryCalculator.computeSummary(report);
        assertNotNull(summary);
        assertEquals(0, summary.getTotal());
    }
}
