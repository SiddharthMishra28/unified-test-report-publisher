package com.example.normalizer.config;

import lombok.Data;
import java.util.Map;

@Data
public class UploadConfig {
    private String endpoint;
    private int timeoutSeconds = 30;
    private int retries = 2;
    private Map<String, String> auth;
}
