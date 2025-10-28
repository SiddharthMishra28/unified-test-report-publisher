package com.example.normalizer.parser.impl;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.Summary;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class JUnitXmlParserTest {

    @Test
    public void testJUnitXmlParsing() throws Exception {
        JUnitXmlParser parser = new JUnitXmlParser();
        File file = new File("src/test/resources/TEST-MySuite.xml");

        NormalizedReport report = parser.parse(file);
        Summary summary = report.getSummary();

        assertEquals("JUnit", report.getFramework());
        assertEquals(3, summary.getTotal());
        assertEquals(1, summary.getFailed());
        assertEquals(1, summary.getSkipped());
        assertEquals(1, summary.getPassed());
        assertTrue(report.getSuites().get(0).getDurationMs() > 0);
    }
}
