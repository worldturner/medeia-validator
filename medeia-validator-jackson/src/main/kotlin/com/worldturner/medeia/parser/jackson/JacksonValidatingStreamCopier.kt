package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.worldturner.medeia.parser.AbstractValidatingStreamCopier
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.InputStream
import java.io.OutputStream

class JacksonValidatingStreamCopier(
    source: InputStream,
    target: OutputStream,
    validator: SchemaValidator,
    inputSourceName: String?,
    private val jsonFactory: JsonFactory
) : AbstractValidatingStreamCopier(source, target, validator, inputSourceName) {
    private val parser = createParser()

    private fun createParser(): JsonParser {
        val jsonParser = jsonFactory.createParser(ValidatorInputStream())
        val consumer = SchemaValidatingConsumer(validator)
        return JacksonTokenDataJsonParser(
            consumer = consumer,
            jsonParser = jsonParser,
            inputSourceName = inputSourceName
        )
    }

    fun copy() {
        while (parser.nextToken() != null) {
        }
    }
}
