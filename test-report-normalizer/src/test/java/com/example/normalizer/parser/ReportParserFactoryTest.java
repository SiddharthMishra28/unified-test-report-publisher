package com.example.normalizer.parser;

import com.example.normalizer.model.NormalizedReport;
import org.junit.jupiter.api.Test;
import java.io.File;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportParserFactoryTest {

    @Test
    public void testFactoryDetectsCucumberParser() throws Exception {
        File fakeReport = new File("src/test/resources/cucumber-results.json");
        NormalizedReport report = ReportParserFactory.parse(fakeReport);
        assertEquals("Cucumber", report.getFramework());
    }

    @Test
    public void testFactoryDetectsJUnitParser() throws Exception {
        File fakeReport = new File("src/test/resources/TEST-MySuite.xml");
        NormalizedReport report = ReportParserFactory.parse(fakeReport);
        assertEquals("JUnit", report.getFramework());
    }

    @Test
    public void testListPlugins() {
        java.util.List<com.example.normalizer.plugin.ParserPluginMetadata> plugins = ReportParserFactory.listPlugins();
        assertFalse(plugins.isEmpty());
        assertTrue(plugins.stream().anyMatch(p -> p.getFramework().equals("JUnit")));
        assertTrue(plugins.stream().anyMatch(p -> p.getFramework().equals("Cucumber")));
        assertTrue(plugins.stream().anyMatch(p -> p.getFramework().equals("TestNG")));
    }
}
