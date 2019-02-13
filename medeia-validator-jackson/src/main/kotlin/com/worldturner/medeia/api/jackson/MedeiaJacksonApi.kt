package com.worldturner.medeia.api.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
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
    private val jsonFactory: JsonFactory = JsonFactory(),
    private val addBuffers: Boolean = true
) : MedeiaApiBase() {

    fun decorateJsonParser(validator: SchemaValidator, jsonParser: JsonParser): JsonParser {
        val consumer = SchemaValidatingConsumer(validator)
        return JacksonTokenDataJsonParser(consumer = consumer, jsonParser = jsonParser)
    }

    fun decorateJsonGenerator(validator: SchemaValidator, jsonGenerator: JsonGenerator): JsonGenerator {
        val consumer = SchemaValidatingConsumer(validator)
        return JacksonTokenDataJsonGenerator(consumer = consumer, delegate = jsonGenerator)
    }

    override fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter {
        val jsonParser =
            streamParser(source)
                ?: readerParser(source)
                ?: throw IllegalArgumentException()
        return JacksonTokenDataJsonParser(consumer = consumer, jsonParser = jsonParser)
    }

    override fun createTokenDataConsumerWriter(destination: Writer): JsonTokenDataConsumer =
        JacksonTokenDataWriter(jsonFactory.createGenerator(destination))

    private fun readerParser(source: SchemaSource): JsonParser? =
        source.reader?.let { if (addBuffers) BufferedReader(it) else it }.let { jsonFactory.createParser(it) }

    private fun streamParser(source: SchemaSource): JsonParser? =
        source.stream?.let { if (addBuffers) BufferedInputStream(it) else it }.let { jsonFactory.createParser(it) }
}