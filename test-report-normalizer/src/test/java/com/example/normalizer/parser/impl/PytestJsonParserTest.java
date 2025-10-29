package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.Summary;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class PytestJsonParserTest {

    @Test
    public void testPytestJsonParsing() throws Exception {
        PytestJsonParser parser = new PytestJsonParser();
        File file = new File("src/test/resources/pytest-results.json");

        NormalizedReport report = parser.parse(file);
        Summary summary = report.getSummary();

        assertEquals("Pytest", report.getFramework());
        assertEquals(3, summary.getTotal());
        assertEquals(1, summary.getFailed());
        assertEquals(1, summary.getPassed());
        assertEquals(1, summary.getSkipped());
    }
}
