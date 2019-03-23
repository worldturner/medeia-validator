package com.worldturner.medeia.parser.gson

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken

fun gsonParseAll(reader: JsonReader) {
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