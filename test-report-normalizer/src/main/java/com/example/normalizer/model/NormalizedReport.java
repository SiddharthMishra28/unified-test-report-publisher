package com.example.normalizer.model;

import lombok.Data;
import java.util.List;

@Data
public class NormalizedReport {
    private String framework;          // e.g. "JUnit5", "Cucumber", "PyTest"
    private RunInfo runInfo;           // Global run metadata
    private Summary summary;           // Aggregated result summary
    private List<TestSuite> suites;    // List of test suites or feature files
}
