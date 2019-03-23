package com.worldturner.medeia.parser.gson

import com.google.gson.stream.JsonReader
import com.worldturner.medeia.parser.AbstractValidatingStreamCopier
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

class GsonValidatingStreamCopier(
    source: InputStream,
    target: OutputStream,
    validator: SchemaValidator,
    inputSourceName: String?
) : AbstractValidatingStreamCopier(source, target, validator, inputSourceName) {
    private val jsonReader = createGsonJsonReader()

    private fun createGsonJsonReader(): JsonReader {
        val reader = InputStreamReader(ValidatorInputStream(), Charsets.UTF_8)
        val consumer = SchemaValidatingConsumer(validator)
        return GsonJsonReaderDecorator(input = reader, consumer = consumer, inputSourceName = inputSourceName)
    }

    fun copy() {
        gsonParseAll(jsonReader)
    }
}
