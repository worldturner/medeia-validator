package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.parser.type.PropertyType
import com.worldturner.medeia.parser.type.ReferenceType
import com.worldturner.medeia.parser.type.TextType
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import org.junit.Test
import kotlin.test.assertEquals

data class GraphNode(
    val name: String,
    val links: List<GraphNode>
)

val graphNode = """
{
    "name": "root",
    "links": [
        {
            "name": "aType",
            "links": []
        },
        {
            "name": "bType",
            "links": [
                {
                    "name": "c",
                    "links": []
                }
            ]
        }
    ]
}
"""

object GraphNodeType : ObjectType(
    kotlinClass = GraphNode::class,
    propertyTypes = listOf(
        PropertyType("name", TextType),
        PropertyType("links", ArrayType(ReferenceType { GraphNodeType }))
    )
)

class SimpleObjectMapperSelfReferenceTest {

    @Test
    fun `Parse graph`() {
        val graphNode =
            parse(GraphNodeType, graphNode, JsonParserLibrary.JACKSON) as GraphNode
        assertEquals("root", graphNode.name)
        assertEquals(2, graphNode.links.size)
        assertEquals(
            listOf(
                GraphNode("aType", emptyList()),
                GraphNode("bType", listOf(GraphNode("c", emptyList())))
            ), graphNode.links
        )
    }
}
