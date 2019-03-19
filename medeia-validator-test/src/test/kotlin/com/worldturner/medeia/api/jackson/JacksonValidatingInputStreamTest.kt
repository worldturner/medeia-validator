package com.worldturner.medeia.api.jackson

import com.worldturner.medeia.api.MetaSchemaInputSource
import com.worldturner.medeia.api.MetaSchemaSource
import com.worldturner.medeia.parser.jackson.JacksonValidatingInputStream
import org.junit.Assert
import org.junit.Test

class JacksonValidatingInputStreamTest {
    val medeia = MedeiaJacksonApi()

    @Test
    fun `Data loaded through JacksonValidatingInputStream should equal the original`() {
        val validator = medeia.loadSchemas(listOf(MetaSchemaSource.DRAFT07))
        val inputSource = MetaSchemaInputSource.DRAFT07
        val validatedBytes = inputSource.stream.use {
            val inputStream = JacksonValidatingInputStream(it, validator, medeia.jsonFactory)
            inputStream.readBytes()
        }
        val originalBytes = inputSource.stream.use { it.readBytes() }
        Assert.assertArrayEquals(originalBytes, validatedBytes)
    }
}