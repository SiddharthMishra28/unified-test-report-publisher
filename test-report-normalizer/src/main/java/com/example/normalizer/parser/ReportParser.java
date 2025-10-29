package com.example.normalizer.parser;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.plugin.ParserPluginMetadata;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ReportParser {
    boolean canParse(File file);
    NormalizedReport parse(File file) throws IOException;

    default ParserPluginMetadata getMetadata() {
        ParserPluginMetadata meta = new ParserPluginMetadata();
        meta.setFramework(this.getClass().getSimpleName().replace("Parser", ""));
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(List.of(".json", ".xml"));
        meta.setVersion("1.0");
        meta.setDescription("Generic report parser");
        return meta;
    }
}
