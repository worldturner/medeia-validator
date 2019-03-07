package com.worldturner.medeia.examples.kotlin.gson.objects

import com.google.gson.Gson
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.gson.MedeiaGsonApi
import com.worldturner.medeia.examples.kotlin.domain.Person
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class ReadObjectExample {
    private val api = MedeiaGsonApi()
    private val gson = Gson()

    fun parseValidExample() {
        val validator = loadSchema()
        val dataReader = InputStreamReader(
            javaClass.getResourceAsStream("/readobject/valid-person.json"),
            StandardCharsets.UTF_8
        )
        val validatedReader = api.createJsonReader(validator, dataReader)
        val person = gson.fromJson<Person>(validatedReader, Person::class.java)
        println(person.firstName)
    }

    fun parseInvalidExample() {
        val validator = loadSchema()
        val dataReader = InputStreamReader(
            javaClass.getResourceAsStream("/readobject/invalid-person.json"),
            StandardCharsets.UTF_8
        )
        val validatedReader = api.createJsonReader(validator, dataReader)
        try {
            gson.fromJson<Person>(validatedReader, Person::class.java)
            throw IllegalStateException("Invalid json data passed validation")
        } catch (e: ValidationFailedException) {
            // Expected
            println("Validation failed as expected: $e")
        }
    }

    private fun loadSchema(): SchemaValidator {
        val source = UrlSchemaSource(
            javaClass.getResource("/readobject/person-address-schema.json")
        )
        return api.loadSchema(source)
    }
}

fun main(args: Array<String>) {
    ReadObjectExample().apply {
        parseValidExample()
        parseInvalidExample()
    }
}