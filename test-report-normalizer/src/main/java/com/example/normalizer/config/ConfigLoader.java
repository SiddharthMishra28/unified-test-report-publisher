package com.example.normalizer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;

public class ConfigLoader {
    public static NormalizerConfig load(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        NormalizerConfig config = mapper.readValue(file, NormalizerConfig.class);

        // Environment variable substitution
        if (config.getUpload() != null && config.getUpload().getToken() != null) {
            config.getUpload().setToken(substituteVariables(config.getUpload().getToken()));
        }
        if (config.getMetadata() != null) {
            config.getMetadata().replaceAll((k, v) -> substituteVariables(v));
        }

        return config;
    }

    private static String substituteVariables(String value) {
        if (value == null || !value.startsWith("${") || !value.endsWith("}")) {
            return value;
        }
        String key = value.substring(2, value.length() - 1);

        // Prioritize system properties (e.g., for testing)
        String substitutedValue = System.getProperty(key);
        if (substitutedValue != null) {
            return substitutedValue;
        }

        // Fallback to environment variables
        substitutedValue = System.getenv(key);

        // Return the original placeholder if no value is found
        return substitutedValue != null ? substitutedValue : value;
    }
}
