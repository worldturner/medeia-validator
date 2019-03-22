package com.worldturner.medeia.api

import com.worldturner.medeia.api.gson.MedeiaGsonApi
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.util.ByteArraySource
import com.worldturner.util.RepeatingByteArrayInputStream
import com.worldturner.util.repeat
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

val prefix = "[".toByteArray()
val infix = ", ".toByteArray()
val suffix = "true]".toByteArray()

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
        val validator = medeia.loadSchemas(listOf(StringSchemaSource(schema), MetaSchemaSource.DRAFT07))
        val schemaBytes = MetaSchemaInputSource.DRAFT07.stream.use { it.readBytes() }
        val repeatsOfSchema = 1237
        val sources = listOf(ByteArraySource(prefix)) +
            listOf(ByteArraySource(schemaBytes), ByteArraySource(infix)).repeat(repeatsOfSchema) +
            ByteArraySource(suffix)

        val originalBytes = RepeatingByteArrayInputStream(sources).use { it.readBytes() }
        val inputSource = StreamInputSource(stream = RepeatingByteArrayInputStream(sources))
        val validatedBytes = ByteArrayOutputStream().use {
            medeia.copyStream(inputSource, it, validator)
            it.toByteArray()
        }

        Assert.assertArrayEquals(originalBytes, validatedBytes)
    }
}
