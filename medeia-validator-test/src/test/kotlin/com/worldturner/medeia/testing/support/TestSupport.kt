package com.worldturner.medeia.testing.support

import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.SimpleObjectMapper
import com.worldturner.medeia.parser.SimpleTreeBuilder
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonParser
import com.worldturner.medeia.parser.jackson.jsonFactory
import com.worldturner.medeia.parser.type.MapperType
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader

fun parse(type: MapperType, input: InputStream, library: JsonParserLibrary): Any? =
    parse(type, BufferedReader(InputStreamReader(input, Charsets.UTF_8)), library)

fun parse(type: MapperType, input: String, library: JsonParserLibrary): Any? =
    parse(type, StringReader(input), library)

fun parse(type: MapperType, input: Reader, library: JsonParserLibrary): Any? {
    val consumer = SimpleObjectMapper(type, 0)
    val parser: JsonParserAdapter = when (library) {
        JsonParserLibrary.JACKSON -> JacksonTokenDataJsonParser(
            jsonParser = jsonFactory.createParser(input), consumer = consumer, inputSourceName = null
        )
        JsonParserLibrary.GSON -> GsonJsonReaderDecorator(
            input = input, consumer = consumer, inputSourceName = null
        )
    }
    parser.parseAll()
    return consumer.takeResult()
}

fun parseTree(input: Reader, library: JsonParserLibrary): TreeNode {
    val consumer = SimpleTreeBuilder(0)
    val parser: JsonParserAdapter = when (library) {
        JsonParserLibrary.JACKSON -> JacksonTokenDataJsonParser(
            jsonParser = jsonFactory.createParser(input), consumer = consumer, inputSourceName = null
        )
        JsonParserLibrary.GSON -> GsonJsonReaderDecorator(
            input = input, consumer = consumer, inputSourceName = null
        )
    }
    parser.parseAll()
    return consumer.takeResult() as TreeNode
}

enum class JsonParserLibrary {
    JACKSON, GSON
}