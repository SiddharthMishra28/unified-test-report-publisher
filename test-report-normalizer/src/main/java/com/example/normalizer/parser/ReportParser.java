package com.example.normalizer.parser;

import com.example.normalizer.model.NormalizedReport;
import java.io.File;
import java.io.IOException;

/**
 * Base contract for all test report parsers.
 * Each framework-specific parser (Cucumber, JUnit, TestNG, etc.)
 * must implement this interface.
 */
public interface ReportParser {

    /**
     * Determines whether this parser can process the given file.
     * @param file The input test report file.
     * @return true if compatible, false otherwise.
     */
    boolean canParse(File file);

    /**
     * Parses the file and converts it into a NormalizedReport.
     * @param file The report file to parse.
     * @return NormalizedReport - standardized structure.
     * @throws IOException if reading/parsing fails.
     */
    NormalizedReport parse(File file) throws IOException;
}
