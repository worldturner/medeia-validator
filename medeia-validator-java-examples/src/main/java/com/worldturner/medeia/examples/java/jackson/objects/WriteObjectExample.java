package com.worldturner.medeia.examples.java.jackson.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.worldturner.medeia.api.SchemaSource;
import com.worldturner.medeia.api.UrlSchemaSource;
import com.worldturner.medeia.api.ValidationFailedException;
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi;
import com.worldturner.medeia.schema.validation.SchemaValidator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;

public class WriteObjectExample {

    private static MedeiaJacksonApi api = new MedeiaJacksonApi();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private Person createValidFixture() {
        Person person = new Person();
        person.setFirstName("Yon");
        person.setLastName("Yonson");
        Address address = new Address();
        address.setRegion("Wisconsin");
        address.setStreet1("1 Lumberjack Road");
        address.setCity("Townsville");
        address.setPostalCode("34216");
        address.setCountry("USA");
        person.setAddress(address);
        return person;
    }

    private Person createInvalidFixture() {
        Person person = createValidFixture();
        person.getAddress().setStreet1(null);
        return person;
    }

    private void writeValidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        StringWriter s = new StringWriter();
        JsonGenerator unvalidatedGenerator = objectMapper.getFactory().createGenerator(s);
        JsonGenerator validatedGenerator = api.decorateJsonGenerator(validator, unvalidatedGenerator);
        objectMapper.writeValue(validatedGenerator, createValidFixture());
        System.out.println(s);
    }

    private void writeInvalidExample() throws IOException {
        SchemaValidator validator = loadSchema();
        StringWriter s = new StringWriter();
        JsonGenerator unvalidatedGenerator = objectMapper.getFactory().createGenerator(s);
        JsonGenerator validatedGenerator = api.decorateJsonGenerator(validator, unvalidatedGenerator);

        try {
            objectMapper.writeValue(validatedGenerator, createInvalidFixture());
            throw new IllegalStateException("Objects that generate Invalid json data passed validation");
        } catch (JsonMappingException e) {
            if (e.getCause() instanceof ValidationFailedException) {
                // Expected
                System.out.println("Validation failed as expected: " + e.getCause());
            } else {
                throw e;
            }
        }
    }

    @NotNull
    private SchemaValidator loadSchema() {
        SchemaSource source = new UrlSchemaSource(
                getClass().getResource("/readobject/person-address-schema.json"));
        return api.loadSchema(source);
    }

    public static void main(String[] args) throws IOException {
        WriteObjectExample example = new WriteObjectExample();
        example.writeValidExample();
        example.writeInvalidExample();
    }
}
