package com.worldturner.medeia.examples.kotlin.jackson.objects

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.examples.kotlin.domain.Person
import com.worldturner.medeia.schema.validation.SchemaValidator

class ReadObjectExample {
    private val api = MedeiaJacksonApi()
    private val objectMapper = jacksonObjectMapper()

    fun parseValidExample() {
        val validator = loadSchema()
        val unvalidatedParser =
            objectMapper.factory.createParser(javaClass.getResource("/readobject/valid-person.json"))
        val validatedParser = api.decorateJsonParser(validator, unvalidatedParser)
        val person = objectMapper.readValue(validatedParser, Person::class.java)
        System.out.println(person.firstName)
    }

    fun parseInvalidExample() {
        val validator = loadSchema()
        val unvalidatedParser =
            objectMapper.factory.createParser(javaClass.getResource("/readobject/invalid-person.json"))
        val validatedParser = api.decorateJsonParser(validator, unvalidatedParser)
        try {
            objectMapper.readValue(validatedParser, Person::class.java)
            throw IllegalStateException("Invalid json data passed validation")
        } catch (e: JsonMappingException) {
            if (e.cause is ValidationFailedException) {
                // Expected
                println("Validation failed as expected: " + e.cause)
            } else {
                throw e
            }
        }
    }

    private fun loadSchema(): SchemaValidator {
        val source = UrlSchemaSource(
            javaClass.getResource("/readobject/person-address-schema.json")
        )
        return api.loadSchema(source)
    }
}

fun main() {
    ReadObjectExample().apply {
        parseValidExample()
        parseInvalidExample()
    }
}