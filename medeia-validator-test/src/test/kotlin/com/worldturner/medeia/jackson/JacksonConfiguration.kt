package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

val jsonFactory = JsonFactory()

val mapper: ObjectMapper = ObjectMapper().also { mapper ->
    mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
    mapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    mapper.registerModule(KotlinModule())
}
