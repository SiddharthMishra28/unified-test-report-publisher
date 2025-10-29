# Test Report Normalizer

A framework-agnostic Java library and command-line interface (CLI) for parsing, normalizing, and uploading test execution reports from various frameworks into a unified JSON format.

## Key Features

-   **Multi-Framework Support**: Out-of-the-box support for JUnit, TestNG, Cucumber, Mocha, Pytest, and Serenity.
-   **Extensible Plugin Architecture**: Easily add new parsers for any other framework by dropping in a JAR. No core code changes needed.
-   **Configuration Driven**: All settings are managed via a simple `normalizer-config.yaml` file, with support for environment variable substitution.
-   **Rich Metadata**: Enriches normalized reports with a schema version, timestamps, and custom metadata (e.g., build ID, git commit).
-   **JSON Schema Validation**: Ensures all generated output conforms to a strict, versioned JSON schema.
-   **Robust Uploader**: A built-in HTTP client uploads the final JSON bundle to any API endpoint, with support for various authentication methods and automatic retries.
-   **Standalone Executable**: The project is packaged as a single, executable JAR with all dependencies included.

## Getting Started

### Prerequisites

-   Java 17 or higher
-   Apache Maven 3.6+

### Building from Source

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/SiddharthMishra28/unified-test-report-publisher.git
    cd unified-test-report-publisher/test-report-normalizer
    ```

2.  **Build the executable JAR:**
    Run the Maven `package` command from the root of the `test-report-normalizer` directory.

    ```bash
    mvn package
    ```

    This will compile the code, run all tests, and create a standalone executable JAR file in the `target/` directory, named `test-report-normalizer-1.0.0.jar`.

## Usage

The application is run as an executable JAR and is configured via a YAML file.

### 1. Create a Configuration File

Create a `normalizer-config.yaml` file to define your settings.

**Example `normalizer-config.yaml`:**

```yaml
# Directory containing your raw test report files (e.g., .xml, .json)
inputDir: ./reports

# Path where the final normalized JSON bundle will be saved
outputFile: ./output/normalized.json

# (Optional) Configuration for uploading the final bundle
upload:
  endpoint: https://qa-dashboard.company.com/api/ingest
  authType: BEARER # Can be NONE, BASIC, BEARER, API_KEY, or CUSTOM_HEADER
  token: ${UPLOAD_TOKEN} # Value will be substituted from the UPLOAD_TOKEN environment variable

# (Optional) Custom metadata to include in the final report bundle
metadata:
  buildId: ${CI_PIPELINE_ID}
  gitCommit: ${GIT_COMMIT_SHA}
  environment: staging
  project: "User Platform"
```

### 2. Run the CLI

Execute the JAR from your terminal, passing the path to your configuration file.

```bash
java -jar target/test-report-normalizer-1.0.0.jar --config normalizer-config.yaml
```

The CLI will:
1.  Scan the `inputDir` for all report files.
2.  Parse each supported file into a normalized format.
3.  Aggregate all results into a single JSON bundle.
4.  Add the specified metadata.
5.  Save the bundle to `outputFile`.
6.  Validate the bundle against the JSON schema.
7.  If configured, upload the bundle to the specified endpoint.

### Listing Available Plugins

To see which parser plugins are currently available (both built-in and from the `plugins/` directory), run:

```bash
java -jar target/test-report-normalizer-1.0.0.jar --list-plugins
```

## Extending with New Parsers (Plugin Development)

The normalizer is designed to be easily extended. You can add support for a new test framework by creating a simple Java project that implements the `ReportParser` interface.

### Step 1: Create a New Maven Project

Create a new Java project. Its `pom.xml` only needs a dependency on the `test-report-normalizer` library itself (once published) and any libraries required for parsing (e.g., a JSON or XML library).

### Step 2: Implement the `ReportParser` Interface

Create a class that implements `com.example.normalizer.parser.ReportParser`.

**Example `MyFrameworkParser.java`:**

```java
package com.mycompany.parser;

import com.example.normalizer.model.NormalizedReport;
import com.example.normalizer.parser.ReportParser;
import com.example.normalizer.plugin.ParserPluginMetadata;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MyFrameworkParser implements ReportParser {

    @Override
    public boolean canParse(File file) {
        // Your logic to determine if this parser can handle the file
        // e.g., check file name, extension, or content.
        return file.getName().endsWith(".myframework.json");
    }

    @Override
    public NormalizedReport parse(File file) throws IOException {
        // Your parsing logic here.
        // Read the file, extract the data, and populate a NormalizedReport object.
        NormalizedReport report = new NormalizedReport();
        report.setFramework("MyFramework");
        // ... populate suites, test cases, etc. ...
        return report;
    }

    @Override
    public ParserPluginMetadata getMetadata() {
        ParserPluginMetadata meta = new ParserPluginMetadata();
        meta.setFramework("MyFramework");
        meta.setParserClass(this.getClass().getName());
        meta.setSupportedExtensions(List.of(".myframework.json"));
        meta.setVersion("1.0");
        meta.setDescription("Parses reports from MyFramework.");
        return meta;
    }
}
```

### Step 3: Register the Parser with `ServiceLoader`

Create a file in your plugin's resources directory at:
`src/main/resources/META-INF/services/com.example.normalizer.parser.ReportParser`

This file should contain the fully qualified name of your implementation class:

```
com.mycompany.parser.MyFrameworkParser
```

### Step 4: Package and Deploy

Build your plugin project into a JAR file. Then, simply drop this JAR into a `plugins/` directory next to the main `test-report-normalizer.jar`.

```
/app
  |- test-report-normalizer-1.0.0.jar
  |- /plugins
      |- my-framework-parser-1.0.jar
```

The next time you run the normalizer, it will automatically discover and use your new parser.

## Sample Report Formats & Normalized Output

### Sample Input: Cucumber JSON

```json
[
  {
    "name": "User Login Feature",
    "elements": [
      {
        "name": "Valid login scenario",
        "type": "scenario",
        "steps": [
          {"name": "Given user is on login page", "result": {"status": "passed", "duration": 1000000000}},
          {"name": "When user enters valid credentials", "result": {"status": "passed", "duration": 2000000000}}
        ]
      }
    ]
  }
]
```

### Sample Input: JUnit XML

```xml
<testsuite name="MySuite" tests="2" failures="1" time="3.5">
    <testcase classname="MyClass" name="testOne" time="1.2"/>
    <testcase classname="MyClass" name="testTwo" time="2.3">
        <failure message="Assertion failed"/>
    </testcase>
</testsuite>
```

### Sample Input: TestNG XML

```xml
<testng-results skipped="0" failed="1" total="2" passed="1">
  <suite name="EndToEndSuite" duration-ms="3000">
    <test name="UserLoginTests">
      <class name="com.example.LoginTests">
        <test-method name="testValidLogin" status="PASS" duration-ms="1200"/>
        <test-method name="testInvalidLogin" status="FAIL" duration-ms="1800"/>
      </class>
    </test>
  </suite>
</testng-results>
```

### Sample Input: Mocha JSON (mochawesome format)

```json
{
  "stats": {
    "suites": 1,
    "tests": 2,
    "passes": 1,
    "failures": 1,
    "duration": 2000
  },
  "results": [
    {
      "title": "My Test Suite",
      "tests": [
        {
          "title": "A passing test",
          "duration": 1500,
          "state": "passed"
        },
        {
          "title": "A failing test",
          "duration": 500,
          "state": "failed",
          "err": {
            "message": "AssertionError: expected true to be false"
          }
        }
      ]
    }
  ]
}
```

### Sample Input: Pytest JSON (pytest-json-report format)

```json
{
  "summary": {
    "total": 3,
    "passed": 1,
    "failed": 1,
    "skipped": 1
  },
  "tests": [
    {
      "nodeid": "test_app.py::test_success",
      "outcome": "passed",
      "call": { "duration": 0.01 }
    },
    {
      "nodeid": "test_app.py::test_failure",
      "outcome": "failed",
      "call": { "duration": 0.01, "longrepr": "AssertionError: assert False" }
    },
    {
      "nodeid": "test_app.py::test_skipped",
      "outcome": "skipped"
    }
  ]
}
```

### Sample Input: Serenity BDD JSON

```json
{
  "name": "Login Feature",
  "testSteps": [
    {
      "description": "User logs in successfully",
      "duration": 5000,
      "result": "SUCCESS"
    },
    {
      "description": "User fails to log in",
      "duration": 3000,
      "result": "FAILURE",
      "exception": {
        "message": "Expected error message was not displayed"
      }
    }
  ]
}
```

### Final Normalized JSON Output

This is an example of the final `NormalizedReportBundle` that is written to the output file and uploaded.

```json
{
  "bundleId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "schemaVersion": "1.0",
  "createdAt": "2025-10-29T12:00:00Z",
  "metadata": {
    "buildId": "12345",
    "gitCommit": "abcdef123",
    "environment": "staging"
  },
  "reports": [
    {
      "framework": "Cucumber",
      "suites": [
        {
          "name": "User Login Feature",
          "durationMs": 3000,
          "tests": [
            {
              "name": "Valid login scenario",
              "status": "PASSED",
              "durationMs": 3000,
              "steps": [
                {"name": "Given user is on login page", "status": "PASSED", "durationMs": 1000},
                {"name": "When user enters valid credentials", "status": "PASSED", "durationMs": 2000}
              ]
            }
          ]
        }
      ]
    },
    {
      "framework": "JUnit",
      "suites": [
        {
          "name": "MySuite",
          "durationMs": 3500,
          "tests": [
            {"name": "testOne", "status": "PASSED", "durationMs": 1200},
            {"name": "testTwo", "status": "FAILED", "durationMs": 2300, "errorMessage": "Assertion failed"}
          ]
        }
      ]
    }
  ]
}
```
