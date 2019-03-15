package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.parser.type.PropertyType
import com.worldturner.medeia.parser.type.SimpleTreeType
import com.worldturner.medeia.parser.type.TextType
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import org.junit.Test
import java.math.BigInteger
import kotlin.test.assertEquals

data class Thing(
    val name: String,
    val tree: TreeNode
)

val treeWithSingle = """
{
    "name": "treeWithSingle",
    "tree": 4
}
"""

val treeWithList = """
{
    "name": "treeWithList",
    "tree": [4]
}
"""

val treeWithObject = """
{
    "name": "treeWithObject",
    "tree": { "foo": "bar" }
}
"""

val treeWithComplex = """
{
    "name": "treeWithComplex",
    "tree": { "foo": ["bar", { "ying": "yang" }] }
}
"""

object ThingType : ObjectType(
    kotlinClass = Thing::class,
    propertyTypes = listOf(
        PropertyType("name", TextType),
        PropertyType("tree", SimpleTreeType)
    )
)

class SimpleObjectMapperSimpleTreeTest {

    @Test
    fun `Parse tree with single`() {
        val thing = parse(ThingType, treeWithSingle, JsonParserLibrary.JACKSON) as Thing
        assertEquals("treeWithSingle", thing.name)
        assertEquals(
            SimpleNode(
                JsonTokenData(
                    type = JsonTokenType.VALUE_NUMBER,
                    integer = BigInteger.valueOf(4L)
                )
            ), thing.tree
        )
    }

    @Test
    fun `Parse tree with list`() {
        val thing = parse(ThingType, treeWithList, JsonParserLibrary.GSON) as Thing
        assertEquals("treeWithList", thing.name)
        assertEquals(
            ArrayNode(
                nodes = mutableListOf(
                    SimpleNode(
                        JsonTokenData(
                            type = JsonTokenType.VALUE_NUMBER,
                            integer = BigInteger.valueOf(4L)
                        )
                    )
                )
            ), thing.tree
        )
    }

    @Test
    fun `Parse tree with object`() {
        val thing = parse(ThingType, treeWithObject, JsonParserLibrary.JACKSON) as Thing
        assertEquals("treeWithObject", thing.name)
        assertEquals(
            ObjectNode(
                nodes = mutableMapOf(
                    "foo" to
                        SimpleNode(JsonTokenData.createText("bar"))
                )
            ),
            thing.tree
        )
    }

    @Test
    fun `Parse tree with complex`() {
        val thing = parse(ThingType, treeWithComplex, JsonParserLibrary.GSON) as Thing
        assertEquals("treeWithComplex", thing.name)
    }
}
