package com.example.normalizer.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.InputStream;
import java.util.Set;

public class SchemaValidator {

    private static final JsonSchemaFactory SCHEMA_FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    public static boolean validate(JsonNode jsonNode) throws Exception {
        try (InputStream schemaStream = SchemaValidator.class.getResourceAsStream("/normalized-schema.json")) {
            if (schemaStream == null) {
                throw new RuntimeException("Could not find schema resource: /normalized-schema.json");
            }
            JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaStream);
            Set<ValidationMessage> messages = schema.validate(jsonNode);
            if (!messages.isEmpty()) {
                System.err.println("❌ Schema validation failed:");
                messages.forEach(msg -> System.err.println("  - " + msg.getMessage()));
                return false;
            }
            return true;
        }
    }
}
