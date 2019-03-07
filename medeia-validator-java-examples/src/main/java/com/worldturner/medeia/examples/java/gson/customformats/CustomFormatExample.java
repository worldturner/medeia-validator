package com.worldturner.medeia.examples.java.gson.customformats;

import com.google.gson.stream.JsonReader;
import com.worldturner.medeia.api.*;
import com.worldturner.medeia.api.gson.MedeiaGsonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
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

    private static MedeiaGsonApi api = new MedeiaGsonApi();

    private void parseValidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        Reader dataReader =
                new InputStreamReader(
                        getClass().getResourceAsStream("/customformats/valid-data.json"),
                        StandardCharsets.UTF_8);
        JsonReader validatedReader = api.createJsonReader(validator, dataReader);
        api.parseAll(validatedReader);
    }

    private void parseInvalidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        Reader dataReader =
                new InputStreamReader(
                        getClass().getResourceAsStream("/customformats/invalid-data.json"),
                        StandardCharsets.UTF_8);
        JsonReader validatedReader = api.createJsonReader(validator, dataReader);
        try {
            api.parseAll(validatedReader);
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
