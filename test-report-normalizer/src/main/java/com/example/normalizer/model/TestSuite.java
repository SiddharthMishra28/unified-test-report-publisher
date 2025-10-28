package com.example.normalizer.model;

import lombok.Data;
import java.util.List;

@Data
public class TestSuite {
    private String name;               // e.g. "Login Feature", "Regression Suite"
    private String packageName;        // optional: com.example.tests
    private Long durationMs;
    private List<TestCase> tests;
}
