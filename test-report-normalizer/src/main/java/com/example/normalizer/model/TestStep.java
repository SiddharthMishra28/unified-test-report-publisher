package com.example.normalizer.model;

import lombok.Data;

@Data
public class TestStep {
    private String name;               // Step or sub-test description
    private String status;             // PASSED, FAILED, SKIPPED
    private Long durationMs;
    private String errorMessage;
}
