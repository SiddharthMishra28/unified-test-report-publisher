package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.Summary;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class MochaJsonParserTest {

    @Test
    public void testMochaJsonParsing() throws Exception {
        MochaJsonParser parser = new MochaJsonParser();
        File file = new File("src/test/resources/mocha-results.json");

        NormalizedReport report = parser.parse(file);
        Summary summary = report.getSummary();

        assertEquals("Mocha", report.getFramework());
        assertEquals(3, summary.getTotal());
        assertEquals(1, summary.getFailed());
        assertEquals(2, summary.getPassed());
        assertEquals(0, summary.getSkipped());

        // There are 2 suites in the sample file (1 root, 1 nested)
        assertEquals(2, report.getSuites().size());
    }
}
