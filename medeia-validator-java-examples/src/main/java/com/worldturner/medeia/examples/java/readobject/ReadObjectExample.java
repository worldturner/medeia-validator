package com.worldturner.medeia.examples.java.readobject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldturner.medeia.api.SchemaSource;
import com.worldturner.medeia.api.UrlSchemaSource;
import com.worldturner.medeia.api.ValidationFailedException;
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ReadObjectExample {

    private static MedeiaJacksonApi api = new MedeiaJacksonApi();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private void parseValidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        JsonParser unvalidatedParser =
                objectMapper.getFactory().createParser(getClass().getResource("/readobject/valid-person.json"));
        JsonParser validatedParser = api.decorateJsonParser(validator, unvalidatedParser);
        Person person = objectMapper.readValue(validatedParser, Person.class);
        System.out.println(person.getFirstName());
    }

    private void parseInvalidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        JsonParser unvalidatedParser =
                objectMapper.getFactory().createParser(getClass().getResource("/readobject/invalid-person.json"));
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
                getClass().getResource("/readobject/person-address-schema.json"));
        return api.loadSchema(source);
    }

    public static void main(String[] args) throws IOException {
        ReadObjectExample example = new ReadObjectExample();
        example.parseValidExample();
        example.parseInvalidExample();
    }
}
