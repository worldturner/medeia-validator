package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.type.BooleanType
import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.parser.type.PropertyType
import com.worldturner.medeia.parser.type.TextType
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import org.junit.Test
import kotlin.test.assertEquals

data class AdditionalThing(
    val name: String,
    val additional: Map<String, Boolean>
)

val thingWithoutAdditional = """
{
    "name": "thingWithoutAdditional"
}
"""

val thingWithAdditional = """
{
    "name": "thingWithAdditional",
    "foo": true,
    "bar": false
}
"""

object AdditionalThingType : ObjectType(
    kotlinClass = AdditionalThing::class,
    propertyTypes = listOf(
        PropertyType("name", TextType)
    ),
    additionalPropertiesType = BooleanType,
    kotlinAdditionalPropertiesProperty = "additional"
)

class SimpleObjectMapperAdditionalPropertiesTest {

    @Test
    fun `Parse thing without additional`() {
        val thing =
            parse(AdditionalThingType, thingWithoutAdditional, JsonParserLibrary.JACKSON) as AdditionalThing
        assertEquals("thingWithoutAdditional", thing.name)
        assertEquals(emptyMap(), thing.additional)
    }

    @Test
    fun `Parse thing with additional`() {
        val thing =
            parse(AdditionalThingType, thingWithAdditional, JsonParserLibrary.GSON) as AdditionalThing
        assertEquals("thingWithAdditional", thing.name)
        assertEquals(mapOf("foo" to true, "bar" to false), thing.additional)
    }
}
