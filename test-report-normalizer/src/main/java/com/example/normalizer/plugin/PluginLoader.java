package com.example.normalizer.plugin;

import com.example.normalizer.parser.ReportParser;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class PluginLoader {

    public static List<ReportParser> loadExternalPlugins(File pluginDir) {
        List<ReportParser> loadedParsers = new ArrayList<>();

        if (!pluginDir.exists() || !pluginDir.isDirectory()) {
            System.out.println("No plugin directory found at: " + pluginDir.getAbsolutePath());
            return loadedParsers;
        }

        File[] jars = pluginDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            System.out.println("No plugin JARs found in: " + pluginDir.getAbsolutePath());
            return loadedParsers;
        }

        try {
            for (File jar : jars) {
                URL jarUrl = jar.toURI().toURL();
                // Use the context classloader as the parent to provide access to the ReportParser interface
                try (URLClassLoader cl = new URLClassLoader(new URL[]{jarUrl}, Thread.currentThread().getContextClassLoader())) {
                    ServiceLoader<ReportParser> serviceLoader = ServiceLoader.load(ReportParser.class, cl);
                    for (ReportParser parser : serviceLoader) {
                        loadedParsers.add(parser);
                        System.out.printf("✅ Loaded external parser: %s from %s%n",
                                parser.getClass().getName(), jar.getName());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error loading external plugins: " + e.getMessage());
        }

        return loadedParsers;
    }
}
