package com.worldturner.medeia.api.gson

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.worldturner.medeia.api.MedeiaApiBase
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.gson.GsonTokenDataReader
import com.worldturner.medeia.parser.gson.GsonTokenDataWriter
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.Writer

class MedeiaGsonApi(private val addBuffers: Boolean = true) : MedeiaApiBase() {

    fun createJsonReader(validator: SchemaValidator, reader: Reader): JsonReader {
        val consumer = SchemaValidatingConsumer(validator.createInstance(0))
        return GsonTokenDataReader(consumer = consumer, input = reader)
    }

    fun createJsonWriter(validator: SchemaValidator, writer: Writer): JsonWriter {
        val consumer = SchemaValidatingConsumer(validator.createInstance(0))
        return GsonTokenDataWriter(consumer = consumer, output = writer)
    }

    override fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter {
        val reader = decorateReader(source) ?: decorateInputStream(source)
        ?: throw IllegalArgumentException()
        return GsonTokenDataReader(consumer = consumer, input = reader)
    }

    private fun decorateInputStream(source: SchemaSource): Reader? =
        source.stream
            ?.let { if (addBuffers) BufferedInputStream(it) else it }
            .let { InputStreamReader(it, Charsets.UTF_8) }

    private fun decorateReader(source: SchemaSource): Reader? =
        source.reader?.let { if (addBuffers) BufferedReader(it) else it }
}