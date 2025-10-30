package com.example.normalizer.cli;

import com.example.normalizer.Publisher;
import com.example.normalizer.config.ConfigLoader;
import com.example.normalizer.config.NormalizerConfig;
import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.NormalizedReportBundle;
import com.example.normalizer.parser.ReportParserFactory;
import com.example.normalizer.telemetry.TelemetryLogger;
import com.example.normalizer.validation.SchemaValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

        File configFile = new File(args[1]);
        if (!configFile.exists()) {
            TelemetryLogger.logError("Config file not found: " + configFile.getAbsolutePath());
            System.exit(2);
        }

        NormalizerConfig config = ConfigLoader.load(configFile);
        File inputDir = new File(config.getInputDir());
        File outputFile = new File(config.getOutputFile());

        if (!inputDir.isDirectory()) {
            TelemetryLogger.logError("Input path is not a directory: " + inputDir.getAbsolutePath());
            System.exit(2);
        }

        List<File> reportFiles = findReportFiles(inputDir);
        if (reportFiles.isEmpty()) {
            TelemetryLogger.logWarning("No report files found in " + inputDir.getAbsolutePath());
            return;
        }

        NormalizedReportBundle bundle = new NormalizedReportBundle();
        for (File reportFile : reportFiles) {
            try {
                TelemetryLogger.parser("Parsing file: " + reportFile.getAbsolutePath());
                NormalizedReport report = ReportParserFactory.parse(reportFile);
                bundle.addReport(report);
            } catch (Exception e) {
                TelemetryLogger.logError("Failed to parse file: " + reportFile.getAbsolutePath() + " - " + e.getMessage());
            }
        }
        TelemetryLogger.normalizer("Normalization complete. " + bundle.getReports().size() + " reports processed.");

        // Publisher is now called before saving the file to get the 'published' status
        boolean published = Publisher.publishReport(config, bundle);

        // Enrich metadata after publishing attempt
        Map<String, Object> metadata = gatherMetadata(config, published);
        bundle.setMetadata(metadata);

        // Now, save the final, enriched bundle to a file
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, bundle);
        TelemetryLogger.logSuccess("Aggregated and normalized report bundle written to: " + outputFile.getAbsolutePath());

        // Validate the final output
        JsonNode jsonNode = mapper.readTree(outputFile);
        if (!SchemaValidator.validate(jsonNode)) {
            TelemetryLogger.logError("Normalized JSON failed schema validation!");
            System.exit(5);
        }
        TelemetryLogger.logSuccess("Schema validation passed!");

        // Print summary
        TelemetryLogger.summary(config.getApplicationName(), published);
    }

    private static Map<String, Object> gatherMetadata(NormalizerConfig config, boolean published) {
        Map<String, Object> metadata = new HashMap<>();
        if (config.getMetadata() != null) {
            metadata.putAll(config.getMetadata());
        }

        metadata.put("application", config.getApplicationName());
        metadata.put("runId", UUID.randomUUID().toString().substring(0, 8));
        metadata.put("runDate", Instant.now().toString());
        metadata.put("ciPipelineId", System.getenv("CI_PIPELINE_ID"));
        metadata.put("commitHash", System.getenv("CI_COMMIT_SHA"));
        metadata.put("pipelineUrl", System.getenv("CI_PIPELINE_URL"));
        if(config.getGitlab() != null){
            metadata.put("gitBranch", config.getGitlab().getRepoBranch());
            metadata.put("gitlabProjectId", config.getGitlab().getGitlabProjectId());
        }
        metadata.put("published", published);

        return metadata;
    }

    private static List<File> findReportFiles(File directory) throws IOException {
        return Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
