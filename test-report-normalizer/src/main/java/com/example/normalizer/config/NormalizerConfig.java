package com.example.normalizer.config;

import com.example.normalizer.uploader.UploadConfig;
import java.util.Map;

public class NormalizerConfig {
    private String inputDir;
    private String outputFile;
    private UploadConfig upload;
    private Map<String, String> metadata;

    public String getInputDir() { return inputDir; }
    public void setInputDir(String inputDir) { this.inputDir = inputDir; }

    public String getOutputFile() { return outputFile; }
    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }

    public UploadConfig getUpload() { return upload; }
    public void setUpload(UploadConfig upload) { this.upload = upload; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
