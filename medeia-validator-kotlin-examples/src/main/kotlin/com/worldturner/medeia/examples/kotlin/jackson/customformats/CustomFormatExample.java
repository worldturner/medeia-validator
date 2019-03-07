package com.worldturner.medeia.examples.kotlin.jackson.customformats;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.worldturner.medeia.api.*;
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomFormatExample {
    static class PalindromeValidator implements FormatValidation {
        @Nullable
        @Override
        public String validate(@Nullable Object value, @NotNull String format) {
            String text = String.valueOf(value);
            String nospaces = text.replaceAll("\\s+", "");
            String lowercase = nospaces.toLowerCase(Locale.US);
            StringBuffer reversed = new StringBuffer(lowercase);
            reversed.reverse();
            if (!lowercase.equals(reversed.toString())) {
                return "not a palindrome";
            }
            return null;
        }
    }

    private static MedeiaJacksonApi api = new MedeiaJacksonApi();
    private static JsonFactory jsonFactory = new JsonFactory();

    private void parseValidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        JsonParser unvalidatedParser =
                jsonFactory.createParser(getClass().getResource("/customformats/valid-data.json"));
        JsonParser validatedParser = api.decorateJsonParser(validator, unvalidatedParser);
        api.parseAll(validatedParser);
    }

    private void parseInvalidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        JsonParser unvalidatedParser =
                jsonFactory.createParser(getClass().getResource("/customformats/invalid-data.json"));
        JsonParser validatedParser = api.decorateJsonParser(validator, unvalidatedParser);
        try {
            api.parseAll(validatedParser);
            throw new IllegalStateException("Invalid json data passed validation");
        } catch (ValidationFailedException e) {
            // Expected
            System.out.println("Validation failed as expected: " + e);
        }
    }

    @NotNull
    private SchemaValidator loadSchema() {
        SchemaSource source = new UrlSchemaSource(
                getClass().getResource("/customformats/customformats-schema.json"));
        Map<String, FormatValidation> customFormats = new HashMap<>();
        customFormats.put("palindrome", new PalindromeValidator());
        return api.loadSchemas(
                Arrays.asList(source),
                new ValidationOptions().withCustomFormats(customFormats));
    }

    public static void main(String[] args) throws IOException {
        CustomFormatExample example = new CustomFormatExample();
        example.parseValidExample();
        example.parseInvalidExample();
    }
}
