package com.worldturner.medeia.schema.loading

import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.ReaderSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationOptions
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import org.hamcrest.Matchers.stringContainsInOrder
import org.junit.Assert.assertThat
import org.junit.Test
import java.io.StringReader

class SchemaLoadingErrorReporting {

    @Test(expected = ValidationFailedException::class)
    fun testLoadStrangeSchema() {
        val medeia = MedeiaJacksonApi()
        val r = StringReader(
            """
{
    "minItems": "foobar"
}
            """.trimIndent()
        )
        val source = ReaderSchemaSource(r, version = JsonSchemaVersion.DRAFT07, name = "string")
        try {
            medeia.loadSchemas(listOf(source), options = ValidationOptions(validateSchema = true))
        } catch (e: ValidationFailedException) {
            assertThat(e.failures.first().location, stringContainsInOrder("at 2:25 "))
            println(e)
            throw e
        }
    }
}