package com.example.normalizer.cli;

import com.example.normalizer.NormalizerService;
import com.example.normalizer.parser.ReportParserFactory;
import com.example.normalizer.telemetry.TelemetryLogger;

import java.io.File;

public class NormalizerCli {
    public static void main(String[] args) throws Exception {
        ReportParserFactory.loadExternal(new File("./plugins"));

        if (args.length > 0 && args[0].equals("--list-plugins")) {
            TelemetryLogger.log("Discovered Parser Plugins:");
            ReportParserFactory.listPlugins().forEach(p ->
                    System.out.printf(" - [%s] %s (supports %s)%n",
                            p.getFramework(), p.getParserClass(), String.join(", ", p.getSupportedExtensions()))
            );
            System.exit(0);
        }

        if (args.length != 2 || !args[0].equals("--config")) {
            TelemetryLogger.logError("Usage: java -jar normalizer.jar --config <path-to-config.yaml>");
            TelemetryLogger.logError("   or: java -jar normalizer.jar --list-plugins");
            System.exit(1);
        }

        NormalizerService normalizerService = new NormalizerService();
        normalizerService.normalizeAndPublish(new File(args[1]));
    }
}
