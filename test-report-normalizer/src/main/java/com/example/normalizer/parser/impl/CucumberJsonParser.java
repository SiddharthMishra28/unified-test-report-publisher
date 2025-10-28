package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import java.io.File;
import java.io.IOException;

public class CucumberJsonParser implements ReportParser {

    @Override
    public boolean canParse(File file) {
        return file.getName().endsWith(".json") && file.getName().contains("cucumber");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        // Actual parsing logic will be implemented in future tasks
        System.out.println("Parsing with CucumberJsonParser (stub)");
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Cucumber");
        return report;
    }
}
