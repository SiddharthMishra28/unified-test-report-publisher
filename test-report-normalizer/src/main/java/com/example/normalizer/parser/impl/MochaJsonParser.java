package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import java.io.File;
import java.io.IOException;

public class MochaJsonParser implements ReportParser {

    @Override
    public boolean canParse(File file) {
        return file.getName().endsWith(".json") && file.getName().contains("mocha");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        System.out.println("Parsing with MochaJsonParser (stub)");
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Mocha");
        return report;
    }
}
