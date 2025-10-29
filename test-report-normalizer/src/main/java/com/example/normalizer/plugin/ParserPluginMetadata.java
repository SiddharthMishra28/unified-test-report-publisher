package com.example.normalizer.plugin;

import lombok.Data;
import java.util.List;

@Data
public class ParserPluginMetadata {
    private String framework;
    private String parserClass;
    private List<String> supportedExtensions;
    private String version;
    private String description;
}
