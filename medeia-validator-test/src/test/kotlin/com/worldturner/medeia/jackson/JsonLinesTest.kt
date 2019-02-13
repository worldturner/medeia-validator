package com.worldturner.medeia.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.SchemaSources
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.test.util.getResourceAsStream
import org.junit.Test

class JsonLinesTest {
    @Test(expected = ValidationFailedException::class)
    fun `should detect schema validation error on 3rd line`() {
        val resource = "/multiline/multiline-data-invalid-3rd-line.jsonl"
        runValidation(resource)
    }

    @Test
    fun `should pass a valid multiline json file`() {
        val resource = "/multiline/multiline-data-valid.jsonl"
        runValidation(resource)
    }

    private fun runValidation(resource: String) {
        val api = MedeiaJacksonApi()
        val sources = SchemaSources.create(
            JsonSchemaVersion.DRAFT07,
            getResourceAsStream("/multiline/multiline-schema.json")
        )
        val validator = api.loadSchemas(sources)
        val jsonFactory = JsonFactory()

        val parser = api.decorateJsonParser(
            validator,
            jsonFactory.createParser(getResourceAsStream(resource))
        )
        while (parser.nextToken() != null) {
        }
    }
}
