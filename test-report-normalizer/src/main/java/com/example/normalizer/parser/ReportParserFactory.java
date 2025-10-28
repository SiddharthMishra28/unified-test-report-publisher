package com.example.normalizer.parser;

import com.example.normalizer.model.NormalizedReport;
import java.io.File;
import java.io.IOException;
import java.util.ServiceLoader;

/**
 * Factory class to automatically detect and use
 * the correct ReportParser implementation for a given file.
 */
public class ReportParserFactory {

    /**
     * Parses the input file by dynamically detecting the correct parser.
     * Uses Java ServiceLoader for pluggable extensibility.
     */
    public static NormalizedReport parse(File file) throws IOException {
        ServiceLoader<ReportParser> loader = ServiceLoader.load(ReportParser.class);

        for (ReportParser parser : loader) {
            if (parser.canParse(file)) {
                System.out.println("✅ Using parser: " + parser.getClass().getSimpleName());
                return parser.parse(file);
            }
        }

        throw new UnsupportedOperationException("❌ Unsupported report type: " + file.getName());
    }
}
