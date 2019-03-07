package com.worldturner.medeia.examples.kotlin.gson.customformats

import com.worldturner.medeia.api.FormatValidation
import com.worldturner.medeia.api.ValidationOptions
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.gson.MedeiaGsonApi
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.io.InputStreamReader
import java.util.Locale

class CustomFormatExample {
    private val api = MedeiaGsonApi()

    fun parseValidExample() {
        val validator = loadSchema()
        val dataReader = InputStreamReader(
            javaClass.getResourceAsStream("/customformats/valid-data.json"),
            Charsets.UTF_8
        )
        val validatedReader = api.createJsonReader(validator, dataReader)
        api.parseAll(validatedReader)
    }

    fun parseInvalidExample() {
        val validator = loadSchema()
        val dataReader = InputStreamReader(
            javaClass.getResourceAsStream("/customformats/invalid-data.json"),
            Charsets.UTF_8
        )
        val validatedReader = api.createJsonReader(validator, dataReader)
        try {
            api.parseAll(validatedReader)
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