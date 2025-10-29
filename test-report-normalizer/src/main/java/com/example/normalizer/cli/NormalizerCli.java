package com.example.normalizer.cli;

import com.example.normalizer.config.ConfigLoader;
import com.example.normalizer.config.NormalizerConfig;
import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParserFactory;
import com.example.normalizer.uploader.UploadClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class NormalizerCli {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].equals("--list-plugins")) {
            System.out.println("Discovered Parser Plugins:");
            ReportParserFactory.listPlugins().forEach(p ->
                System.out.printf(" - [%s] %s (supports %s)%n",
                    p.getFramework(), p.getParserClass(), String.join(", ", p.getSupportedExtensions()))
            );
            System.exit(0);
        }

        if (args.length != 2 || !args[0].equals("--config")) {
            System.err.println("Usage: java -jar normalizer.jar --config <path-to-config.yaml>");
            System.err.println("   or: java -jar normalizer.jar --list-plugins");
            System.exit(1);
        }

        File configFile = new File(args[1]);
        if (!configFile.exists()) {
            System.err.println("Config file not found: " + configFile.getAbsolutePath());
            System.exit(2);
        }

        NormalizerConfig config = ConfigLoader.load(configFile);

        File inputDir = new File(config.getInputDir());
        File outputFile = new File(config.getOutputFile());

        if (!inputDir.isDirectory()) {
            System.err.println("Input path is not a directory: " + inputDir.getAbsolutePath());
            System.exit(2);
        }

        List<File> reportFiles = findReportFiles(inputDir);
        if (reportFiles.isEmpty()) {
            System.out.println("No report files found in " + inputDir.getAbsolutePath());
            return;
        }

        NormalizedReportBundle bundle = new NormalizedReportBundle();
        if (config.getMetadata() != null) {
            bundle.setMetadata(config.getMetadata());
        }

        for (File reportFile : reportFiles) {
            try {
                System.out.println("Parsing file: " + reportFile.getAbsolutePath());
                NormalizedReport report = ReportParserFactory.parse(reportFile);
                bundle.addReport(report);
            } catch (Exception e) {
                System.err.println("Failed to parse file: " + reportFile.getAbsolutePath() + " - " + e.getMessage());
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, bundle);
        System.out.println("Aggregated and normalized report bundle written to: " + outputFile.getAbsolutePath());

        if (config.getUpload() != null && config.getUpload().getEndpoint() != null) {
            System.out.println("Uploading normalized bundle to " + config.getUpload().getEndpoint());
            boolean success = UploadClient.upload(bundle, config.getUpload());
            if (!success) {
                System.err.println("Upload failed. Exiting with error.");
                System.exit(6);
            }
        } else {
            System.out.println("Upload endpoint not configured. Skipping upload.");
        }
    }

    private static List<File> findReportFiles(File directory) throws IOException {
        return Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
