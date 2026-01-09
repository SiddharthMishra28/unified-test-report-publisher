package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import com.example.normalizer.plugin.ParserPluginMetadata;

import java.io.File;
import java.io.IOException;

public class KarateJsonParser implements ReportParser {

    private final CucumberJsonParser cucumberParser = new CucumberJsonParser();

    @Override
    public boolean canParse(File file) {
        return file.getName().toLowerCase().contains("karate") && file.getName().endsWith(".json");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = cucumberParser.parse(file);
        report.setFramework("Karate");
        return report;
    }

    @Override
    public ParserPluginMetadata getMetadata() {
        ParserPluginMetadata meta = new ParserPluginMetadata();
        meta.setFramework("Karate");
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(java.util.List.of(".json"));
        meta.setVersion("1.0");
        meta.setDescription("Parses Karate JSON report files (which are Cucumber-compatible).");
        return meta;
    }
}
