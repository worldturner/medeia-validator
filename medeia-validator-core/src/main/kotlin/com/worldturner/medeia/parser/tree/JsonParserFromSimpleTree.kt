package com.worldturner.medeia.parser.tree

import com.worldturner.medeia.parser.ArrayNodeData
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.ObjectNodeData
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.TokenNodeData
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.pointer.JsonPointerBuilder
import java.util.ArrayDeque

class JsonParserFromSimpleTree(val tree: NodeData, val consumer: JsonTokenDataAndLocationConsumer) : JsonParserAdapter {
    private val propertyNamesStack = ArrayDeque<MutableSet<String>>()

    inner class DynamicTokenLocation : JsonTokenLocation {
        override val level: Int
            get() = this@JsonParserFromSimpleTree.level
        override val pointer: JsonPointer
            get() = jsonPointerBuilder.toJsonPointer()
        override val propertyNames: Set<String>
            get() = propertyNamesStack.peek() ?: emptySet()
        override val column: Int
            get() = super.column
        override val line: Int
            get() = super.line

        override fun toString(): String {
            return "at $pointer"
        }
    }

    val dynamicLocation = DynamicTokenLocation()

    var level = 0
    val jsonPointerBuilder = JsonPointerBuilder()
    var currentObjectNode: ObjectNodeData? = null

    fun generateEvents(node: NodeData) {
        when (node) {
            is TokenNodeData -> {
                jsonPointerBuilder.consume(node.token)
                consumer.consume(node.token, dynamicLocation)
            }
            is ArrayNodeData -> {
                jsonPointerBuilder.consume(TOKEN_START_ARRAY)
                consumer.consume(TOKEN_START_ARRAY, dynamicLocation)
                level++
                node.nodes.forEach {
                    generateEvents(it)
                }
                level--
                jsonPointerBuilder.consume(TOKEN_END_ARRAY)
                consumer.consume(TOKEN_END_ARRAY, dynamicLocation)
            }
            is ObjectNodeData -> {
                jsonPointerBuilder.consume(TOKEN_START_OBJECT)
                consumer.consume(TOKEN_START_OBJECT, dynamicLocation)
                level++
                propertyNamesStack.addFirst(HashSet())
                node.nodes.forEach {
                    val fieldNameToken = JsonTokenData(JsonTokenType.FIELD_NAME, text = it.key)
                    jsonPointerBuilder.consume(fieldNameToken)
                    consumer.consume(fieldNameToken, dynamicLocation)
                    generateEvents(it.value)
                    propertyNamesStack.peek() += it.key
                }
                propertyNamesStack.removeFirst()
                level--
                currentObjectNode = node
                jsonPointerBuilder.consume(TOKEN_END_OBJECT)
                consumer.consume(TOKEN_END_OBJECT, dynamicLocation)
            }
        }
    }

    override fun parseAll() {
        generateEvents(tree)
    }

    override fun close() {
    }
}