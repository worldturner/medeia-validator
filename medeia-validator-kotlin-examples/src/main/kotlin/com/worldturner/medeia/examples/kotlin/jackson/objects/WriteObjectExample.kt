package com.worldturner.medeia.examples.kotlin.jackson.objects

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.examples.kotlin.domain.createInvalidPersonFixture
import com.worldturner.medeia.examples.kotlin.domain.createValidPersonFixture
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.io.StringWriter

class WriteObjectExample {
    private val api = MedeiaJacksonApi()
    private val objectMapper = jacksonObjectMapper()

    fun writeValidExample() {
        val validator = loadSchema()
        val s = StringWriter()
        val unvalidatedGenerator = objectMapper.factory.createGenerator(s)
        val validatedGenerator = api.decorateJsonGenerator(validator, unvalidatedGenerator)
        objectMapper.writeValue(validatedGenerator, createValidPersonFixture())
        println(s)
    }

    fun writeInvalidExample() {
        val validator = loadSchema()
        val s = StringWriter()
        val unvalidatedGenerator = objectMapper.factory.createGenerator(s)
        val validatedGenerator = api.decorateJsonGenerator(validator, unvalidatedGenerator)

        try {
            objectMapper.writeValue(validatedGenerator, createInvalidPersonFixture())
            throw IllegalStateException("Objects that generate Invalid json data passed validation")
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
    WriteObjectExample().apply {
        writeValidExample()
        writeInvalidExample()
    }
}