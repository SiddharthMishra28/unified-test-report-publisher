package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import java.io.File;
import java.io.IOException;

public class TestNGXmlParser implements ReportParser {

    @Override
    public boolean canParse(File file) {
        return file.getName().endsWith(".xml") && file.getName().contains("testng");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        System.out.println("Parsing with TestNGXmlParser (stub)");
        NormalizedReport report = new NormalizedReport();
        report.setFramework("TestNG");
        return report;
    }
}
