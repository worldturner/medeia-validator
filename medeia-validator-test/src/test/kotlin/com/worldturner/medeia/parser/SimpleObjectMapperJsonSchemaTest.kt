package com.worldturner.medeia.parser

import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import org.junit.Test
import kotlin.test.assertEquals

class SimpleObjectMapperJsonSchemaTest {
    @Test
    fun `Parse metaschema`() {
        val metaSchema =
            this::class.java.getResourceAsStream("/meta-schemas/schema-draft07.json")
        val schema =
            parse(JsonSchemaDraft07Type, metaSchema, JsonParserLibrary.GSON) as JsonSchema
        assertEquals("Core schema meta-schema", schema.title)
    }
}
