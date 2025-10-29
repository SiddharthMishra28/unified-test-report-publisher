package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.Summary;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class SerenityJsonParserTest {

    @Test
    public void testSerenityJsonParsing() throws Exception {
        SerenityJsonParser parser = new SerenityJsonParser();
        File file = new File("src/test/resources/serenity-results.json");

        NormalizedReport report = parser.parse(file);
        Summary summary = report.getSummary();

        assertEquals("Serenity", report.getFramework());
        assertEquals(2, summary.getTotal());
        assertEquals(1, summary.getFailed());
        assertEquals(1, summary.getPassed());
        assertEquals(0, summary.getSkipped());
    }
}
