package com.worldturner.medeia.api.gson

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.worldturner.medeia.api.InputPreference
import com.worldturner.medeia.api.MedeiaApiBase
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.parser.gson.GsonJsonWriterDecorator
import com.worldturner.medeia.parser.gson.GsonTokenDataWriter
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.io.Writer

class MedeiaGsonApi(private val addBuffer: Boolean = true) : MedeiaApiBase() {

    fun createJsonReader(validator: SchemaValidator, reader: Reader): JsonReader {
        val consumer = SchemaValidatingConsumer(validator)
        return GsonJsonReaderDecorator(consumer = consumer, input = reader)
    }

    fun createJsonWriter(validator: SchemaValidator, writer: Writer): JsonWriter {
        val consumer = SchemaValidatingConsumer(validator)
        return GsonJsonWriterDecorator(consumer = consumer, output = writer)
    }

    override fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter {

        val reader = when (source.input.preference) {
            InputPreference.STREAM -> decorateInputStream(source)
            InputPreference.READER -> decorateReader(source)
        }
        return GsonJsonReaderDecorator(consumer = consumer, input = reader)
    }

    override fun createTokenDataConsumerWriter(destination: Writer): JsonTokenDataConsumer =
        GsonTokenDataWriter(JsonWriter(destination))

    fun parseAll(reader: JsonReader) {
        loop@ do {
            var token = reader.peek()!!
            when (token) {
                JsonToken.NUMBER -> reader.nextString()
                JsonToken.STRING -> reader.nextString()
                JsonToken.NAME -> reader.nextName()
                JsonToken.BOOLEAN -> reader.nextBoolean()
                JsonToken.NULL -> reader.nextNull()
                JsonToken.BEGIN_ARRAY -> reader.beginArray()
                JsonToken.END_ARRAY -> reader.endArray()
                JsonToken.BEGIN_OBJECT -> reader.beginObject()
                JsonToken.END_OBJECT -> reader.endObject()
                JsonToken.END_DOCUMENT -> break@loop
            }
        } while (true)
    }

    private fun decorateInputStream(source: SchemaSource): Reader =
        source.input.stream
            .let { if (addBuffer) BufferedInputStream(it) else it }
            .let { InputStreamReader(it, Charsets.UTF_8) }

    private fun decorateReader(source: SchemaSource): Reader =
        source.input.reader.let { if (addBuffer) BufferedReader(it) else it }
}