package com.worldturner.medeia.parser.tree

import com.worldturner.medeia.parser.ArrayNode
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.ObjectNode
import com.worldturner.medeia.parser.SimpleNode
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.pointer.JsonPointerBuilder
import com.worldturner.util.withValue
import java.util.ArrayDeque

class JsonParserFromSimpleTree(
    private val tree: TreeNode,
    private val consumer: JsonTokenDataAndLocationConsumer,
    private val inputSourceName: String?
) : JsonParserAdapter {
    private val propertyNamesStack = ArrayDeque<MutableSet<String>>()

    inner class DynamicTokenLocation : JsonTokenLocation {
        override val level: Int
            get() = this@JsonParserFromSimpleTree.level
        override val pointer: JsonPointer
            get() = jsonPointerBuilder.toJsonPointer()
        override val propertyNames: Set<String>
            get() = propertyNamesStack.peek() ?: emptySet()
        override val column: Int
            get() = currentNode?.column ?: -1
        override val line: Int
            get() = currentNode?.line ?: -1
        override val inputSourceName: String?
            get() = this@JsonParserFromSimpleTree.inputSourceName

        override fun toString(): String {
            val b = StringBuilder("at ")
            if (line != -1) {
                b.append(line)
                if (column != -1)
                    b.append(':').append(column)
                b.append(" (").append(pointer).append(')')
            } else {
                b.append(pointer)
            }
            inputSourceName?.let { b.append(" in ").append(it) }
            return b.toString()
        }
    }

    private val dynamicLocation = DynamicTokenLocation()

    private var level = 0
    private val jsonPointerBuilder = JsonPointerBuilder()
    private var currentNode: TreeNode? = null

    private fun generateEvents(node: TreeNode) {
        ::currentNode.withValue(node) {
            when (node) {
                is SimpleNode -> {
                    jsonPointerBuilder.consume(node.token)
                    consumer.consume(node.token, dynamicLocation)
                }
                is ArrayNode -> {
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
                is ObjectNode -> {
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
                    jsonPointerBuilder.consume(TOKEN_END_OBJECT)
                    consumer.consume(TOKEN_END_OBJECT, dynamicLocation)
                }
            }
        }
    }

    override fun parseAll() {
        generateEvents(tree)
    }

    override fun close() {
    }
}
