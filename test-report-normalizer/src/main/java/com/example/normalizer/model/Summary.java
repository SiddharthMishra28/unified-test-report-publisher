package com.example.normalizer.model;

import lombok.Data;

@Data
public class Summary {
    private int total;                 // Total test count
    private int passed;                // Passed test count
    private int failed;                // Failed test count
    private int skipped;               // Skipped/ignored tests
    private int broken;                // For frameworks with partial failures
    private Double passRate;           // Percentage passed
}
