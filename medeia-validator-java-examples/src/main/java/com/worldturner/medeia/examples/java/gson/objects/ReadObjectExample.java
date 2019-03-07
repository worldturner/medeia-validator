package com.worldturner.medeia.examples.java.gson.objects;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.worldturner.medeia.api.SchemaSource;
import com.worldturner.medeia.api.UrlSchemaSource;
import com.worldturner.medeia.api.ValidationFailedException;
import com.worldturner.medeia.api.gson.MedeiaGsonApi;
import com.worldturner.medeia.examples.java.domain.Person;
import com.worldturner.medeia.schema.validation.SchemaValidator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ReadObjectExample {
    private static MedeiaGsonApi api = new MedeiaGsonApi();
    private static Gson gson = new Gson();

    private void parseValidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        Reader dataReader =
                new InputStreamReader(
                        getClass().getResourceAsStream("/readobject/valid-person.json"),
                        StandardCharsets.UTF_8);
        JsonReader validatedReader = api.createJsonReader(validator, dataReader);
        Person person = gson.fromJson(validatedReader, Person.class);
        System.out.println(person.getFirstName());
    }

    private void parseInvalidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        Reader dataReader =
                new InputStreamReader(
                        getClass().getResourceAsStream("/readobject/invalid-person.json"),
                        StandardCharsets.UTF_8);
        JsonReader validatedReader = api.createJsonReader(validator, dataReader);
        try {
            Person person = gson.fromJson(validatedReader, Person.class);
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
