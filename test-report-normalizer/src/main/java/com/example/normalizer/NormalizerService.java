package com.example.normalizer;

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

public class NormalizerService {

    public boolean normalizeAndPublish(File configFile) throws Exception {
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + configFile.getAbsolutePath());
        }

        ReportParserFactory.loadExternal(new File("./plugins"));

        NormalizerConfig config = ConfigLoader.load(configFile);
        File inputDir = new File(config.getInputDir());
        File outputFile = new File(config.getOutputFile());

        if (!inputDir.isDirectory()) {
            throw new IllegalArgumentException("Input path is not a directory: " + inputDir.getAbsolutePath());
        }

        List<File> reportFiles = findReportFiles(inputDir);
        if (reportFiles.isEmpty()) {
            TelemetryLogger.logWarning("No report files found in " + inputDir.getAbsolutePath());
            return false;
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

        boolean published = Publisher.publishReport(config, bundle);

        Map<String, Object> metadata = gatherMetadata(config, published);
        bundle.setMetadata(metadata);

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, bundle);

        JsonNode jsonNode = mapper.readTree(outputFile);
        if (!SchemaValidator.validate(jsonNode)) {
            throw new IllegalStateException("Normalized JSON failed schema validation");
        }

        TelemetryLogger.summary(config.getApplicationName(), published);
        return published;
    }

    private Map<String, Object> gatherMetadata(NormalizerConfig config, boolean published) {
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
        if (config.getGitlab() != null) {
            metadata.put("gitBranch", config.getGitlab().getRepoBranch());
            metadata.put("gitlabProjectId", config.getGitlab().getGitlabProjectId());
        }
        metadata.put("published", published);

        return metadata;
    }

    private List<File> findReportFiles(File directory) throws IOException {
        return Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
