package com.worldturner.medeia.api.gson

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.worldturner.medeia.api.InputPreference
import com.worldturner.medeia.api.InputSource
import com.worldturner.medeia.api.MedeiaApiBase
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.parser.gson.GsonJsonWriterDecorator
import com.worldturner.medeia.parser.gson.GsonTokenDataWriter
import com.worldturner.medeia.parser.gson.GsonValidatingStreamCopier
import com.worldturner.medeia.parser.gson.gsonParseAll
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Reader
import java.io.Writer

class MedeiaGsonApi(private val addBuffer: Boolean = true) : MedeiaApiBase() {

    override fun copyStream(source: InputSource, target: OutputStream, validator: SchemaValidator) {
        val copier = GsonValidatingStreamCopier(source, target, validator)
        copier.copy()
    }

    fun createJsonReader(validator: SchemaValidator, source: InputSource): JsonReader {
        val consumer = SchemaValidatingConsumer(validator)
        return GsonJsonReaderDecorator(consumer = consumer, input = createReader(source), inputSourceName = source.name)
    }

    fun createJsonReader(validator: SchemaValidator, reader: Reader): JsonReader {
        val consumer = SchemaValidatingConsumer(validator)
        return GsonJsonReaderDecorator(consumer = consumer, input = reader, inputSourceName = null)
    }

    fun createJsonWriter(validator: SchemaValidator, writer: Writer): JsonWriter {
        val consumer = SchemaValidatingConsumer(validator)
        return GsonJsonWriterDecorator(consumer = consumer, output = writer, inputSourceName = null)
    }

    override fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter {
        val reader = createReader(source.input)
        return GsonJsonReaderDecorator(consumer = consumer, input = reader, inputSourceName = source.input.name)
    }

    override fun createTokenDataConsumerWriter(destination: Writer): JsonTokenDataConsumer =
        GsonTokenDataWriter(JsonWriter(destination))

    fun parseAll(reader: JsonReader) {
        gsonParseAll(reader)
    }

    private fun createReader(source: InputSource): Reader =
        when (source.preference) {
            InputPreference.STREAM -> decorateInputStream(source)
            InputPreference.READER -> decorateReader(source)
        }

    private fun decorateInputStream(source: InputSource): Reader =
        source.stream
            .let { if (addBuffer) BufferedInputStream(it) else it }
            .let { InputStreamReader(it, Charsets.UTF_8) }

    private fun decorateReader(source: InputSource): Reader =
        source.reader.let { if (addBuffer) BufferedReader(it) else it }
}