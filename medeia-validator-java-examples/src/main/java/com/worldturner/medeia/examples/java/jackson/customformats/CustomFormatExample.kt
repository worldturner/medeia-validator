package com.worldturner.medeia.examples.java.jackson.customformats

import com.fasterxml.jackson.core.JsonFactory
import com.worldturner.medeia.api.FormatValidation
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationOptions
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.util.Locale

class CustomFormatExample {
    private val api = MedeiaJacksonApi()
    private val jsonFactory = JsonFactory()

    fun parseValidExample() {
        val validator = loadSchema()
        val unvalidatedParser = jsonFactory.createParser(javaClass.getResource("/customformats/valid-data.json"))
        val validatedParser = api.decorateJsonParser(validator, unvalidatedParser)
        api.parseAll(validatedParser)
    }

    fun parseInvalidExample() {
        val validator = loadSchema()
        val unvalidatedParser = jsonFactory.createParser(javaClass.getResource("/customformats/invalid-data.json"))
        val validatedParser = api.decorateJsonParser(validator, unvalidatedParser)
        try {
            api.parseAll(validatedParser)
            throw IllegalStateException("Invalid json data passed validation")
        } catch (e: ValidationFailedException) {
            // Expected
            println("Validation failed as expected: $e")
        }
    }

    private fun loadSchema(): SchemaValidator {
        val source = UrlSchemaSource(
            javaClass.getResource("/customformats/customformats-schema.json")
        )
        val customFormats = mapOf("palindrome" to PalindromeValidator())
        return api.loadSchemas(listOf(source), ValidationOptions(customFormats = customFormats))
    }
}

class PalindromeValidator : FormatValidation {
    override fun validate(value: Any?, format: String): String? {
        val text = value.toString()
        val nospaces = text.replace("\\s+".toRegex(), "")
        val lowercase = nospaces.toLowerCase(Locale.US)
        val reversed = StringBuffer(lowercase).also { it.reverse() }.toString()
        return if (lowercase != reversed) "not a palindrome" else null
    }
}

fun main(args: Array<String>) {
    val example = CustomFormatExample()
    example.parseValidExample()
    example.parseInvalidExample()
}