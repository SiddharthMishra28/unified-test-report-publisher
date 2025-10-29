package com.example.dummy;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import com.example.normalizer.plugin.ParserPluginMetadata;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DummyParser implements ReportParser {
    @Override
    public boolean canParse(File file) {
        return file.getName().endsWith(".dummy");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        NormalizedReport report = new NormalizedReport();
        report.setFramework("Dummy");
        return report;
    }

    @Override
    public ParserPluginMetadata getMetadata() {
        ParserPluginMetadata meta = new ParserPluginMetadata();
        meta.setFramework("Dummy");
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(List.of(".dummy"));
        meta.setVersion("1.0");
        meta.setDescription("A dummy parser for testing.");
        return meta;
    }
}
