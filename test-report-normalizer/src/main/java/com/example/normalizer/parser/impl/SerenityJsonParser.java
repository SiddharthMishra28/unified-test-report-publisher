package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import java.io.File;
import java.io.IOException;

public class SerenityJsonParser implements ReportParser {

    @Override
    public boolean canParse(File file) {
        return file.getName().endsWith(".json") && file.getName().contains("serenity");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        System.out.println("Parsing with SerenityJsonParser (stub)");
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Serenity");
        return report;
    }
}
