package com.worldturner.medeia.api

import com.worldturner.medeia.api.gson.MedeiaGsonApi
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.testing.support.RepeatingByteArrayInputStream
import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream

val schema = """
{
    "${"$"}schema": "http://json-schema.org/draft-07/schema#",
    "type": "array",
    "items": {
        "${"$"}ref": "http://json-schema.org/draft-07/schema#"
    }
}
""".trimIndent()
class ValidatingStreamCopyTest {
    val medeiaJackson = MedeiaJacksonApi()
    val medeiaGson = MedeiaGsonApi()

    @Test
    fun `Data copied and validated through copyStream should equal the original Jackson`() =
        `Data copied and validated through copyStream should equal the original`(medeiaJackson)

    @Test
    fun `Data copied and validated through copyStream should equal the original Gson`() =
        `Data copied and validated through copyStream should equal the original`(medeiaGson)

    private fun `Data copied and validated through copyStream should equal the original`(medeia: MedeiaApiBase) {
        val validator = medeia.loadSchemas(listOf(MetaSchemaSource.DRAFT07))
        val schemaSource = MetaSchemaInputSource.DRAFT07.stream.use { it.readBytes() }
        val inputSource = StreamInputSource(
            stream = RepeatingByteArrayInputStream(schemaSource, 100)
        )
        val validatedBytes = ByteArrayOutputStream().use {
            medeia.copyStream(inputSource, it, validator)
            it.toByteArray()
        }
        val originalBytes = RepeatingByteArrayInputStream(schemaSource, 100).use { it.readBytes() }
        Assert.assertArrayEquals(originalBytes, validatedBytes)
    }
}
