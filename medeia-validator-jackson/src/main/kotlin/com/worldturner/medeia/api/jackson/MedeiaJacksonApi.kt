package com.worldturner.medeia.api.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.worldturner.medeia.api.InputPreference
import com.worldturner.medeia.api.InputSource
import com.worldturner.medeia.api.MedeiaApiBase
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonGenerator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonParser
import com.worldturner.medeia.parser.jackson.JacksonTokenDataWriter
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.Writer

class MedeiaJacksonApi @JvmOverloads constructor(
    val jsonFactory: JsonFactory =
        JsonFactory().apply { enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION) },
    private val addBuffer: Boolean = true
) : MedeiaApiBase() {

    fun createJsonParser(validator: SchemaValidator, source: InputSource): JsonParser {
        val jsonParser = createJsonParser(source)
        val consumer = SchemaValidatingConsumer(validator)
        return JacksonTokenDataJsonParser(consumer = consumer, jsonParser = jsonParser, inputSourceName = source.name)
    }

    fun decorateJsonParser(validator: SchemaValidator, jsonParser: JsonParser): JsonParser {
        val consumer = SchemaValidatingConsumer(validator)
        val sourceName = jsonParser.tokenLocation.sourceRef?.toString()
        return JacksonTokenDataJsonParser(consumer = consumer, jsonParser = jsonParser, inputSourceName = sourceName)
    }

    fun decorateJsonGenerator(validator: SchemaValidator, jsonGenerator: JsonGenerator): JsonGenerator {
        val consumer = SchemaValidatingConsumer(validator)
        return JacksonTokenDataJsonGenerator(consumer = consumer, delegate = jsonGenerator, inputSourceName = null)
    }

    override fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter {
        val jsonParser = createJsonParser(source.input)
        return JacksonTokenDataJsonParser(
            consumer = consumer,
            jsonParser = jsonParser,
            inputSourceName = source.input.name
        )
    }

    fun parseAll(parser: JsonParser) {
        while (parser.nextToken() != null) {
        }
    }

    override fun createTokenDataConsumerWriter(destination: Writer): JsonTokenDataConsumer =
        JacksonTokenDataWriter(jsonFactory.createGenerator(destination))

    private fun createJsonParser(source: InputSource): JsonParser =
        when (source.preference) {
            InputPreference.STREAM -> streamParser(source)
            InputPreference.READER -> readerParser(source)
        }

    private fun readerParser(source: InputSource): JsonParser =
        source.reader.let { if (addBuffer) BufferedReader(it) else it }.let { jsonFactory.createParser(it) }

    private fun streamParser(source: InputSource): JsonParser =
        source.stream.let { if (addBuffer) BufferedInputStream(it) else it }.let { jsonFactory.createParser(it) }
}