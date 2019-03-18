package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonParser
import com.worldturner.medeia.parser.jackson.jsonFactory
import org.junit.Test
import kotlin.test.assertEquals

class TextOutputBuilderTest {
    val input =
        "[{\"foo\":\"bar\",\"bar\":\"baz\"},{\"bar\":\"baz\",\"foo\":\"bar\"}]"

    @Test
    fun test() {
        val builder = TextOutputBuilder()
        val parser = JacksonTokenDataJsonParser(jsonFactory.createParser(input), builder, inputSourceName = null)
        parser.parseAll()
        assertEquals(input, builder.takeResult())
    }
}