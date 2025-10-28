package com.example.normalizer.parser;

import com.example.normalizer.model.NormalizedReport;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportParserFactoryTest {

    @Test
    public void testFactoryDetectsCucumberParser() throws Exception {
        File fakeReport = new File("cucumber-results.json");
        NormalizedReport report = ReportParserFactory.parse(fakeReport);
        assertEquals("Cucumber", report.getFramework());
    }

    @Test
    public void testFactoryDetectsJUnitParser() throws Exception {
        File fakeReport = new File("TEST-MySuite.xml");
        NormalizedReport report = ReportParserFactory.parse(fakeReport);
        assertEquals("JUnit", report.getFramework());
    }
}
