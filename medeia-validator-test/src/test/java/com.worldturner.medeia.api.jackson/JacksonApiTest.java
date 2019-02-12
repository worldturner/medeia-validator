package com.worldturner.medeia.api.jackson;

import com.worldturner.medeia.schema.validation.SchemaValidator;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertNotNull;

public class JacksonApiTest {
    /**
     * Compile-only test, ensures that the correct method signatures from Kotlin code are
     * available in Java code.
     */
    @Test
    public void testKotlinMethodSignaturesFromJava() {
        InputStream stream = new ByteArrayInputStream(new byte[0]);
        new SchemaSource(stream, JsonSchemaVersion.DRAFT07);
        new SchemaSource(stream);
        new SchemaSources(JsonSchemaVersion.DRAFT07, stream);
        new SchemaSources(stream);
        Reader reader = new StringReader("");
        new SchemaSource(reader, JsonSchemaVersion.DRAFT07);
        new SchemaSource(reader);
        new SchemaSources(JsonSchemaVersion.DRAFT07, reader);
        new SchemaSources(reader);

        new JsonSchemaValidationOptions(UniqueItemsValidationMethod.IN_MEMORY_TREES);
        new JsonSchemaValidationOptions(UniqueItemsValidationMethod.IN_MEMORY_TREES, false);
        JsonSchemaValidationOptions.DEFAULT
                .withOptimizeExistentialValidators(false)
                .withUniqueItemsValidationMethod(UniqueItemsValidationMethod.DIGEST_SHA1);
    }

    @Test
    public void testLoadSchema() {
        MedeiaJacksonApi medeia = new MedeiaJacksonApi();
        InputStream stream = getClass().getResourceAsStream("/meta-schemas/schema-draft07.json");
        SchemaSources sources = new SchemaSources(stream);
        SchemaValidator validator = medeia.loadSchemas(sources);
        assertNotNull(validator);
    }
}