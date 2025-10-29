package com.example.normalizer.parser;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.plugin.ParserPluginMetadata;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ReportParserFactory {
    private static final List<ReportParser> PARSERS = loadParsers();

    private static List<ReportParser> loadParsers() {
        ServiceLoader<ReportParser> loader = ServiceLoader.load(ReportParser.class);
        List<ReportParser> list = new ArrayList<>();
        loader.forEach(list::add);
        return list;
    }

    public static void loadExternal(File pluginDir) {
        List<ReportParser> externals = com.example.normalizer.plugin.PluginLoader.loadExternalPlugins(pluginDir);
        if (!externals.isEmpty()) {
            PARSERS.addAll(externals);
            System.out.println("🔌 Merged " + externals.size() + " external parsers into registry.");
        }
    }

    public static NormalizedReport parse(File file) throws IOException {
        for (ReportParser parser : PARSERS) {
            if (parser.canParse(file)) {
                return parser.parse(file);
            }
        }
        throw new IOException("No compatible parser found for file: " + file.getName());
    }

    public static List<ParserPluginMetadata> listPlugins() {
        List<ParserPluginMetadata> list = new ArrayList<>();
        for (ReportParser parser : PARSERS) {
            list.add(parser.getMetadata());
        }
        return list;
    }
}
