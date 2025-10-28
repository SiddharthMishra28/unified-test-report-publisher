package com.example.normalizer.model;

import lombok.Data;
import java.util.List;

@Data
public class TestCase {
    private String name;               // e.g. "User logs in successfully"
    private String className;          // Optional, e.g. com.example.LoginTest
    private String status;             // PASSED, FAILED, SKIPPED, BROKEN
    private Long durationMs;
    private String errorMessage;       // Failure message if any
    private String stackTrace;         // Stack trace snippet
    private List<String> tags;         // Cucumber tags, TestNG groups, etc.
    private List<TestStep> steps;      // Nested steps if applicable
}
