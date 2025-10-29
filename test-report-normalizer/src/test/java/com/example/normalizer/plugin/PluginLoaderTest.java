package com.example.normalizer.plugin;

import com.example.normalizer.parser.ReportParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluginLoaderTest {

    private File pluginDir;
    private File dummyJar;

    @BeforeEach
    public void setUp() throws Exception {
        pluginDir = new File("target/test-plugins");
        pluginDir.mkdirs();
        dummyJar = new File(pluginDir, "dummy-parser.jar");
        createDummyJar();
    }

    @AfterEach
    public void tearDown() {
        dummyJar.delete();
        pluginDir.delete();
    }

    @Test
    public void testLoadExternalPlugins() {
        List<ReportParser> parsers = PluginLoader.loadExternalPlugins(pluginDir);
        assertTrue(parsers.stream().anyMatch(p -> p.getClass().getName().equals("com.example.dummy.DummyParser")));
    }

    private void createDummyJar() throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(dummyJar))) {
            // Add the DummyParser class
            String classPath = "com/example/dummy/DummyParser.class";
            jos.putNextEntry(new JarEntry(classPath));

            Path classFilePath = Paths.get("target/test-classes/" + classPath);
            Files.copy(classFilePath, jos);
            jos.closeEntry();

            // Add the service file
            String serviceFile = "META-INF/services/com.example.normalizer.parser.ReportParser";
            jos.putNextEntry(new JarEntry(serviceFile));
            jos.write("com.example.dummy.DummyParser".getBytes());
            jos.closeEntry();
        }
    }
}
