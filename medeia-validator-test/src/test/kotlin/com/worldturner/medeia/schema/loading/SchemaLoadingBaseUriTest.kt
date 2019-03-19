package com.worldturner.medeia.schema.loading

import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.StringSchemaSource
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.schema.validation.SchemaValidator
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.Matchers.hasKey
import org.junit.Assert.assertThat
import org.junit.Test
import java.net.URI

private val TEST_SCHEMA = """
{
    "${"$"}id": "http://foo.bar/",
    "minItems": 4
}
""".trimIndent()

class SchemaLoadingBaseUriTest {
    val medeia = MedeiaJacksonApi()

    @Test
    fun `Schema loaded with baseUri which has a $id should be accessible by both`() {
        val schemaMap = mutableMapOf<URI, SchemaValidator>()
        val source =
            StringSchemaSource(
                TEST_SCHEMA,
                version = JsonSchemaVersion.DRAFT07,
                baseUri = URI.create("schemas/foobar.json")
            )
        medeia.loadSchemas(listOf(source), schemaMap)
        assertThat(schemaMap, aMapWithSize(object : BaseMatcher<Int>() {
            override fun matches(actual: Any?): Boolean =
                actual as Int >= 3

            override fun describeTo(description: Description) {
                description
                    .appendText("greater than or equal to ")
                    .appendValue(3)
            }
        }))
        assertThat(schemaMap, hasKey(URI.create("http://foo.bar/")))
        assertThat(schemaMap, hasKey(URI.create("http://foo.bar/#")))
        assertThat(schemaMap, hasKey(URI.create("schemas/foobar.json")))
    }
}