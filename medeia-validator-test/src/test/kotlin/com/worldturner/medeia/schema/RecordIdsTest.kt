package com.worldturner.medeia.schema

import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.ValidationBuilderContext
import org.hamcrest.Matchers.hasEntry
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.URI
import kotlin.test.assertEquals

class RecordIdsTest {
    @Test
    fun testRecordedIds() {
        val schema = JsonSchema(
            id = URI("http://example.com/root.json"),
            definitions = mapOf(
                "A" to JsonSchema(
                    id = URI("#foo"),
                    jsonPointer = JsonPointer("/definitions/A")
                ),
                "B" to JsonSchema(
                    id = URI("other.json"),
                    definitions = mapOf(
                        "X" to JsonSchema(
                            id = URI("#bar"),
                            jsonPointer = JsonPointer("/definitions/B/definitions/X")
                        ),
                        "Y" to JsonSchema(
                            id = URI("t/inner.json"),
                            jsonPointer = JsonPointer("/definitions/B/definitions/Y")
                        )
                    ),
                    jsonPointer = JsonPointer("/definitions/B")
                ),
                "C" to JsonSchema(
                    id = URI("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f"),
                    jsonPointer = JsonPointer("/definitions/C")
                )
            ), jsonPointer = JsonPointer("")
        )

        val context = ValidationBuilderContext()
        schema.buildValidator(context)
        val ids = context.ids
        ids.forEach { (uri, schema) ->
            println("$uri -> ${schema.id}")
        }
        val defs = schema.definitions!!
        val defsBdefsX = defs["B"]!!.definitions!!["X"]
        assertThat(ids, hasEntry(URI("http://example.com/root.json"), schema))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#"), schema))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#foo"), defs["A"]))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#/definitions/A"), defs["A"]))
        assertThat(ids, hasEntry(URI("http://example.com/other.json"), defs["B"]))
        assertThat(ids, hasEntry(URI("http://example.com/other.json#"), defs["B"]))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#/definitions/B"), defs["B"]))
        assertThat(ids, hasEntry(URI("http://example.com/other.json#bar"), defsBdefsX))
        assertThat(ids, hasEntry(URI("http://example.com/other.json#/definitions/X"), defsBdefsX))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#/definitions/B/definitions/X"), defsBdefsX))
        val defsBdefsY = defs["B"]!!.definitions!!["Y"]
        assertThat(ids, hasEntry(URI("http://example.com/t/inner.json"), defsBdefsY))
        assertThat(ids, hasEntry(URI("http://example.com/t/inner.json#"), defsBdefsY))
        assertThat(ids, hasEntry(URI("http://example.com/other.json#/definitions/Y"), defsBdefsY))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#/definitions/B/definitions/Y"), defsBdefsY))
        assertThat(ids, hasEntry(URI("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f"), defs["C"]))
        assertThat(ids, hasEntry(URI("urn:uuid:ee564b8a-7a87-4125-8c96-e9f123d6766f#"), defs["C"]))
        assertThat(ids, hasEntry(URI("http://example.com/root.json#/definitions/C"), defs["C"]))
    }

    @Test
    fun relativizeTest() {
        val parent = JsonPointer("/definitions/B")
        val child = JsonPointer("/definitions/B/definitions/X")
        assertEquals(JsonPointer("/definitions/X"), parent.relativize(child))
    }
}