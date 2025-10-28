package com.example.normalizer.util;

import com.example.normalizer.model.*;

public class ReportSummaryCalculator {

    public static Summary computeSummary(NormalizedReport report) {
        Summary summary = new Summary();
        int total = 0, passed = 0, failed = 0, skipped = 0, broken = 0;

        if (report != null && report.getSuites() != null) {
            for (TestSuite suite : report.getSuites()) {
                if (suite != null && suite.getTests() != null) {
                    for (TestCase test : suite.getTests()) {
                        if (test != null && test.getStatus() != null) {
                            total++;
                            switch (test.getStatus().toUpperCase()) {
                                case "PASSED" -> passed++;
                                case "FAILED" -> failed++;
                                case "SKIPPED" -> skipped++;
                                case "BROKEN" -> broken++;
                            }
                        }
                    }
                }
            }
        }

        summary.setTotal(total);
        summary.setPassed(passed);
        summary.setFailed(failed);
        summary.setSkipped(skipped);
        summary.setBroken(broken);
        summary.setPassRate(total > 0 ? (passed * 100.0 / total) : 0.0);

        if (report != null) {
            report.setSummary(summary);
        }

        return summary;
    }
}
