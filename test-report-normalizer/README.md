# Test Report Normalizer

**A framework-agnostic test report normalization library for seamless CI/CD integration.**

Developed by: Siddharth Mishra <connectwithsiddharthm@gmail.com>

---

## 📖 Table of Contents

1.  [Introduction](#-introduction)
2.  [Core Features](#-core-features)
3.  [Getting Started](#-getting-started)
    *   [Prerequisites](#prerequisites)
    *   [Building the Library](#building-the-library)
4.  [Command-Line Usage](#-command-line-usage)
    *   [Running the Normalizer](#running-the-normalizer)
    *   [Listing Available Plugins](#listing-available-plugins)
5.  [Configuration](#-configuration)
    *   [The `normalizer-config.yaml` File](#the-normalizer-configyaml-file)
    *   [Configuration Parameters](#configuration-parameters)
    *   [Sample Configuration](#sample-configuration)
6.  [GitLab Integration](#-gitlab-integration)
    *   [Overview](#overview)
    *   [GitLab Setup](#gitlab-setup)
    *   [CI/CD Environment Variables](#cicd-environment-variables)
7.  [Supported Formats & Output Schema](#-supported-formats--output-schema)
    *   [Supported Input Formats](#supported-input-formats)
    *   [The Normalized JSON Schema](#the-normalized-json-schema)
    *   [Sample JSON Output](#sample-json-output)
8.  [Extending the Normalizer](#-extending-the-normalizer)
    *   [Plugin Architecture](#plugin-architecture)
    *   [Creating a Custom Parser](#creating-a-custom-parser)
9.  [Logging & Telemetry](#-logging--telemetry)
    *   [Log Modes](#log-modes)
    *   [Environment Variables](#environment-variables)
10. [License](#-license)

---

## 📜 Introduction

The **Test Report Normalizer** is a powerful Java-based utility designed to solve a common problem in modern software development: the fragmentation of test report formats. Different testing frameworks (like JUnit, TestNG, Cucumber, Pytest, etc.) produce reports in various formats (XML, JSON, etc.), making it difficult to create a unified view of test results across a project.

This library provides a solution by:

1.  **Parsing** multiple report formats through an extensible plugin system.
2.  **Normalizing** the parsed data into a single, consistent JSON schema.
3.  **Publishing** the unified report to a central location, with first-class support for GitLab.

It is designed to be run in any CI/CD pipeline, providing a single source of truth for your project's test quality.

## ✨ Core Features

*   **Framework-Agnostic:** Parses reports from a wide variety of testing frameworks.
*   **Unified JSON Schema:** Converts all reports into a single, easy-to-understand JSON format.
*   **Extensible Plugin System:** Easily add support for new report formats by creating custom parser plugins.
*   **GitLab Integration:** Publishes normalized reports directly to a GitLab repository, with intelligent create/update logic.
*   **CI/CD Aware:** Automatically enriches reports with metadata from your CI/CD environment (e.g., pipeline ID, commit hash).
*   **Robust & Resilient:** Features include network retries, timeouts, and a local audit trail for publishing.
*   **Configurable Logging:** Provides human-readable colored logs for local development and structured JSON logs for machine processing.

## 🚀 Getting Started

### Prerequisites

*   **Java 17 JDK** (or newer)
*   **Apache Maven** 3.6.0 (or newer)

### Building the Library

The project is packaged as a standalone, executable JAR file, which includes all necessary dependencies.

To build the JAR, run the following command from the root of the project:

```bash
mvn clean package
```

This will produce a file named `test-report-normalizer-1.0.0.jar` in the `target/` directory.

## 💻 Command-Line Usage

The library is controlled via a simple command-line interface (CLI).

### Running the Normalizer

To run the normalization process, you need to provide a configuration file.

```bash
java -jar target/test-report-normalizer-1.0.0.jar --config /path/to/your/normalizer-config.yaml
```

### Listing Available Plugins

To see a list of all built-in and custom parser plugins that the library has discovered, use the `--list-plugins` flag.

```bash
java -jar target/test-report-normalizer-1.0.0.jar --list-plugins
```

This will produce output similar to the following:

```
Discovered Parser Plugins:
 - [Cucumber] com.example.normalizer.parser.impl.CucumberJsonParser (supports .json)
 - [JUnit] com.example.normalizer.parser.impl.JUnitXmlParser (supports .xml)
 - [TestNG] com.example.normalizer.parser.impl.TestNGXmlParser (supports .xml)
 ...
```

## ⚙️ Configuration

The behavior of the normalizer is controlled by a single YAML file.

### The `normalizer-config.yaml` File

This file defines the input directory for your test reports, the output file for the normalized JSON, and the configuration for the GitLab publisher.

### Configuration Parameters

| Parameter                   | Type    | Description                                                                                                                               |
| --------------------------- | ------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| `applicationName`           | String  | A unique name for your application. Used for metadata and in the GitLab upload path.                                                      |
| `inputDir`                  | String  | The path to the directory containing your raw test report files. The tool will recursively scan this directory for reports.                 |
| `outputFile`                | String  | The path where the final, normalized JSON report bundle will be saved.                                                                    |
| `publishOnlyOnCI`           | Boolean | If `true`, the GitLab publishing step will only run if a CI environment is detected (i.e., `CI_PIPELINE_ID` is set). Defaults to `false`.    |
| `metadata`                  | Map     | A map of custom key-value pairs to be added to the metadata section of the final report.                                                    |
| `gitlab`                    | Object  | Configuration for the GitLab publisher. See the [GitLab Integration](#-gitlab-integration) section for details.                               |
| `gitlab.gitlabProjectId`      | String  | The ID of your GitLab project.                                                                                                            |
| `gitlab.gitlabPersonalAccessToken` | String  | A GitLab Personal Access Token with `api` scope. It is **highly recommended** to provide this via an environment variable.             |
| `gitlab.repoBranch`           | String  | The branch to which the report should be published.                                                                                       |
| `gitlab.gitlabUploadFolderPath` | String  | The destination path for the report in your GitLab repository. Supports dynamic placeholders.                                           |

### Sample Configuration

```yaml
# normalizer-config.yaml

applicationName: "my-microservice"

# Directories
inputDir: "./build/reports/tests"
outputFile: "./normalized-report.json"

# CI/CD Settings
publishOnlyOnCI: true

# Custom metadata to be added to the final report
metadata:
  team: "backend-squad"
  environment: "staging"

# GitLab Publisher Configuration
gitlab:
  # It is recommended to use environment variables for sensitive data
  gitlabProjectId: "${GITLAB_PROJECT_ID}" # e.g., 12345
  gitlabPersonalAccessToken: "${GITLAB_API_TOKEN}"
  repoBranch: "main"

  # The path where the report will be saved in the GitLab repo.
  # Available placeholders: {{date}}, {{application}}, {{runHashBundleId}}
  gitlabUploadFolderPath: "data/test-reports/{{date}}/{{application}}/run_{{runHashBundleId}}.json"
```

## 🔗 GitLab Integration

### Overview

The library includes a robust publisher for uploading the normalized report directly to a GitLab repository. This is useful for storing historical test data or feeding data into other systems.

The publisher will:
1.  Check if the report file already exists in the repository.
2.  If it does not exist, it will create the file with a `POST` request.
3.  If it does exist, it will update the file with a `PUT` request.
4.  It will automatically retry the request on transient network errors (e.g., 5xx status codes).

### GitLab Setup

1.  **Create a Personal Access Token:**
    *   In GitLab, go to `User Settings` -> `Access Tokens`.
    *   Create a token with the `api` scope.
    *   **Store this token securely!** It is recommended to save it as a masked CI/CD variable in your GitLab project (e.g., `GITLAB_API_TOKEN`).
2.  **Get your Project ID:**
    *   The Project ID can be found on the main page of your GitLab project, under the project name.

### CI/CD Environment Variables

The library will automatically gather and inject the following GitLab CI/CD variables into the report's metadata if they are available:

*   `CI_PIPELINE_ID`
*   `CI_COMMIT_SHA`
*   `CI_PIPELINE_URL`

## 📊 Supported Formats & Output Schema

### Supported Input Formats

The library includes built-in parsers for the following test report formats:

*   **JUnit** (`.xml`)
*   **TestNG** (`.xml`)
*   **Cucumber** (`.json`)
*   **Mocha** (`.json`)
*   **Pytest** (`.json`)
*   **Serenity** (`.json`)
*   **Karate** (`.json`)
*   **Allure** (`.json`)

### The Normalized JSON Schema

The final output is a single JSON object with the following structure:

```json
{
  "schemaVersion": "1.0.0",
  "bundleId": "a1b2c3d4-e5f6-...",
  "createdAt": "2025-10-30T12:00:00Z",
  "metadata": {
    "application": "my-app",
    "runId": "f0a1b2c3",
    "ciPipelineId": "12345",
    "commitHash": "abcdef123456",
    "pipelineUrl": "...",
    "gitBranch": "main",
    "gitlabProjectId": "67890",
    "published": true
  },
  "reports": [
    {
      "framework": "Cucumber",
      "summary": {
        "total": 10,
        "passed": 8,
        "failed": 1,
        "skipped": 1,
        "durationMs": 5000
      },
      "suites": [
        {
          "name": "Login Feature",
          "durationMs": 5000,
          "tests": [
            {
              "name": "Successful Login",
              "status": "PASSED",
              "durationMs": 2000,
              "steps": [
                {
                  "name": "Given I am on the login page",
                  "status": "PASSED",
                  "durationMs": 500
                },
                {
                  "name": "When I enter valid credentials",
                  "status": "PASSED",
                  "durationMs": 1000
                },
                {
                  "name": "Then I should be redirected to the dashboard",
                  "status": "PASSED",
                  "durationMs": 500
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

### Sample JSON Output

*(A sample output is provided above in the schema description.)*

## 🔌 Extending the Normalizer

### Plugin Architecture

The normalizer uses Java's `ServiceLoader` to discover and load parser implementations at runtime. This means you can easily add support for a new test report format without modifying the core library.

The tool will look for plugins in two places:
1.  On the classpath (for built-in parsers).
2.  In a `plugins/` directory (relative to the JAR file) for external plugins.

### Creating a Custom Parser

To create a custom parser, you need to:

1.  **Implement the `ReportParser` interface:** This interface has a single method, `parse(File file)`, which takes a report file and returns a `NormalizedReport` object.
2.  **Register the parser:** Create a file named `com.example.normalizer.parser.ReportParser` in the `src/main/resources/META-INF/services/` directory of your plugin project. This file should contain the fully qualified class name of your new parser.
3.  **Package as a JAR:** Package your plugin as a JAR file and place it in the `plugins/` directory.

## 📝 Logging & Telemetry

### Log Modes

The library provides three logging modes, which can be configured via environment variables:

| Mode              | Description                                        |
| ----------------- | -------------------------------------------------- |
| **Colored (default)** | Human-readable, colored logs for local development. |
| **Plain**         | Plain text logs for CI/CD environments.            |
| **JSON**          | Structured JSON logs for machine processing.       |

### Environment Variables

*   `NORMALIZER_JSON_LOGS=true`: Enables structured JSON logging.
*   `NORMALIZER_COLOR_LOGS=false`: Disables colored output.

## ⚖️ License

This project is licensed under the terms of the **GNU General Public License v3.0**.

A copy of the license is available in the `LICENSE` file.

---
Copyright (c) 2025, Siddharth Mishra <connectwithsiddharthm@gmail.com>
