package com.example.normalizer;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.model.TestSuite;
import com.example.normalizer.parser.ReportParserFactory;
import com.example.normalizer.uploader.UploadClient;
import com.example.normalizer.uploader.UploadConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NormalizerCli {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java -jar normalizer.jar <input-directory> <output-json-file>");
            System.exit(1);
        }

        File inputDir = new File(args[0]);
        File outputFile = new File(args[1]);

        if (!inputDir.isDirectory()) {
            System.err.println("Input path is not a directory: " + inputDir.getAbsolutePath());
            System.exit(2);
        }

        List<File> reportFiles = findReportFiles(inputDir);
        if (reportFiles.isEmpty()) {
            System.out.println("No report files found in " + inputDir.getAbsolutePath());
            return;
        }

        NormalizedReport finalBundle = new NormalizedReport();
        List<TestSuite> allSuites = new ArrayList<>();

        for (File reportFile : reportFiles) {
            try {
                System.out.println("Parsing file: " + reportFile.getAbsolutePath());
                NormalizedReport report = ReportParserFactory.parse(reportFile);
                if (report.getSuites() != null) {
                    allSuites.addAll(report.getSuites());
                }
            } catch (Exception e) {
                System.err.println("Failed to parse file: " + reportFile.getAbsolutePath() + " - " + e.getMessage());
            }
        }
        finalBundle.setSuites(allSuites);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, finalBundle);
        System.out.println("Aggregated and normalized report written to: " + outputFile.getAbsolutePath());


        String endpoint = System.getenv("UPLOAD_ENDPOINT");
        String token = System.getenv("UPLOAD_TOKEN");
        String username = System.getenv("UPLOAD_USER");
        String password = System.getenv("UPLOAD_PASS");
        String authTypeEnv = System.getenv("UPLOAD_AUTH_TYPE"); // BASIC | BEARER | API_KEY | NONE

        if (endpoint != null && !endpoint.isBlank()) {
            UploadConfig config = new UploadConfig();
            config.setEndpoint(endpoint);
            config.setAuthType(UploadConfig.AuthType.valueOf(authTypeEnv != null ? authTypeEnv.toUpperCase() : "NONE"));

            if (config.getAuthType() == UploadConfig.AuthType.BASIC) {
                config.setUsername(username);
                config.setPassword(password);
            } else {
                config.setToken(token);
            }

            System.out.println("Uploading normalized bundle to " + endpoint);
            boolean success = UploadClient.upload(finalBundle, config);
            if (!success) {
                System.err.println("Upload failed. Exiting with error.");
                System.exit(6);
            }
        } else {
            System.out.println("UPLOAD_ENDPOINT not provided. Skipping upload.");
        }
    }

    private static List<File> findReportFiles(File directory) throws IOException {
        return Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
