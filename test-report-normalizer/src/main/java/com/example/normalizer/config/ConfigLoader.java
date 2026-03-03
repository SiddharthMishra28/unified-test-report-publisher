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
        if (config.getDashboard() != null && config.getDashboard().getAuth() != null) {
            config.getDashboard().getAuth().replaceAll((k, v) -> substituteVariables(v));
        }
        if (config.getGitlab() != null) {
            config.getGitlab().setGitlabProjectId(substituteVariables(config.getGitlab().getGitlabProjectId()));
            config.getGitlab().setGitlabPersonalAccessToken(substituteVariables(config.getGitlab().getGitlabPersonalAccessToken()));
            config.getGitlab().setRepoBranch(substituteVariables(config.getGitlab().getRepoBranch()));
            config.getGitlab().setGitlabUploadFolderPath(substituteVariables(config.getGitlab().getGitlabUploadFolderPath()));
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
