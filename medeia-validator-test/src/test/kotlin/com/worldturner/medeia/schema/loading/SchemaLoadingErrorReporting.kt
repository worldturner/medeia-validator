package com.worldturner.medeia.schema.loading

import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.StringSchemaSource
import com.worldturner.medeia.api.TokenLocationException
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationOptions
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.Assert.assertThat
import org.junit.Test

private val TEST_SCHEMA = """
{
    "minItems": "foobar"
}
""".trimIndent()

class SchemaLoadingErrorReporting {
    val medeia = MedeiaJacksonApi()

    @Test(expected = ValidationFailedException::class)
    fun `Validation error in schema should include line, column and filename`() {
        val source = StringSchemaSource(TEST_SCHEMA, version = JsonSchemaVersion.DRAFT07, name = "input-string")
        try {
            medeia.loadSchemas(listOf(source), options = ValidationOptions(validateSchema = true))
        } catch (e: ValidationFailedException) {
            assertThat(e.failures.first().location, stringContainsInOrder("at 2:25 "))
            assertThat(e.failures.first().location, stringContainsInOrder("in input-string"))
            throw e
        }
    }

    @Test(expected = TokenLocationException::class)
    fun `Object mapping error in schema should include line, column and filename`() {
        val source =
            StringSchemaSource(TEST_SCHEMA, version = JsonSchemaVersion.DRAFT07, name = "input-string")
        try {
            medeia.loadSchemas(listOf(source), options = ValidationOptions(validateSchema = false))
        } catch (e: TokenLocationException) {
            assertThat(e.location, stringContainsInOrder("at 2:25 "))
            assertThat(e.location, stringContainsInOrder("in input-string"))
            throw e
        }
    }
}