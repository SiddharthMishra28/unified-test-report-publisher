package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.Summary;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class KarateJsonParserTest {

    @Test
    public void testKarateJsonParsing() throws Exception {
        KarateJsonParser parser = new KarateJsonParser();
        File file = new File("test-reports/karate-report.json");

        NormalizedReport report = parser.parse(file);
        Summary summary = report.getSummary();

        assertEquals("Karate", report.getFramework());
        assertEquals(1, summary.getTotal());
        assertEquals(0, summary.getFailed());
        assertEquals(1, summary.getPassed());
        assertEquals(0, summary.getSkipped());
        assertTrue(report.getSuites().get(0).getDurationMs() > 0);
    }
}
