package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.type.SimpleTreeType
import java.util.ArrayDeque
import java.util.Deque

class SimpleTreeBuilder(val startLevel: Int) : JsonTokenDataAndLocationBuilder {
    private val stack: Deque<NodeData> = ArrayDeque()
    private var currentProperty: String? = null
    private var result: NodeData? = null

    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        if (token.type == JsonTokenType.FIELD_NAME) {
            currentProperty = token.text!!
        } else {
            if (token.type.firstToken) {
                val top = if (stack.isEmpty()) null else stack.peek()
                val nodeData =
                    when (token.type) {
                        JsonTokenType.START_OBJECT -> ObjectNodeData()
                        JsonTokenType.START_ARRAY -> ArrayNodeData()
                        else -> TokenNodeData(token)
                    }

                when (top) {
                    is ObjectNodeData -> {
                        top.nodes[currentProperty!!] = nodeData
                    }
                    is ArrayNodeData -> {
                        top.nodes += nodeData
                    }
                    else -> {
                    }
                }
                stack.push(nodeData)
            }
            if (token.type.lastToken) {
                val node = stack.pop()!!
                if (location.level == startLevel) {
                    result = node
                }
            }
        }
    }

    override fun takeResult(): NodeData? {
        val r = result
        result = null
        return r
    }
}

sealed class NodeData {
    internal abstract fun isEqualTo(other: NodeData): Boolean

    open fun textChild(fieldName: String): String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NodeData
        return isEqualTo(other)
    }

    override fun toString(): String {
        val textOutputBuilder = TextOutputBuilder()
        SimpleTreeType.write(this, textOutputBuilder)
        return textOutputBuilder.toString()
    }
}

class TokenNodeData(val token: JsonTokenData) : NodeData() {

    override fun isEqualTo(other: NodeData): Boolean {
        return other is TokenNodeData && token == other.token
    }

    override fun hashCode(): Int = token.hashCode()
}

class ArrayNodeData(val nodes: MutableList<NodeData> = mutableListOf()) : NodeData() {
    override fun isEqualTo(other: NodeData): Boolean {
        return other is ArrayNodeData && nodes == other.nodes
    }

    override fun hashCode(): Int = nodes.hashCode()
}

class ObjectNodeData(val nodes: MutableMap<String, NodeData> = mutableMapOf()) : NodeData() {
    override fun isEqualTo(other: NodeData): Boolean {
        return other is ObjectNodeData && nodes == other.nodes
    }

    override fun textChild(fieldName: String): String? {
        val child = nodes[fieldName]
        return if (child is TokenNodeData && child.token.type == JsonTokenType.VALUE_TEXT) {
            child.token.text!!
        } else {
            null
        }
    }

    override fun hashCode(): Int = nodes.hashCode()
}