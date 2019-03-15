package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.type.SimpleTreeType
import java.util.ArrayDeque
import java.util.Deque

class SimpleTreeBuilder(val startLevel: Int) : JsonTokenDataAndLocationBuilder {
    private val stack: Deque<TreeNode> = ArrayDeque()
    private var currentProperty: String? = null
    private var result: TreeNode? = null

    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        if (token.type == JsonTokenType.FIELD_NAME) {
            currentProperty = token.text!!
        } else {
            if (token.type.firstToken) {
                val top = if (stack.isEmpty()) null else stack.peek()
                val nodeData =
                    when (token.type) {
                        JsonTokenType.START_OBJECT -> ObjectNode(location.line, location.column)
                        JsonTokenType.START_ARRAY -> ArrayNode(location.line, location.column)
                        else -> SimpleNode(token, location.line, location.column)
                    }
                when (top) {
                    is ObjectNode -> {
                        top.nodes[currentProperty!!] = nodeData
                    }
                    is ArrayNode -> {
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

    override fun takeResult(): TreeNode? {
        val r = result
        result = null
        return r
    }
}

sealed class TreeNode(
    val line: Int = -1,
    val column: Int = -1
) {
    internal abstract fun isEqualTo(other: TreeNode): Boolean

    /**
     * Quick way to get the value of a text property of an ObjectNode, or null
     * if this is not an ObjectNode or the property doesn't exist.
     */
    open fun textProperty(fieldName: String): String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TreeNode
        return isEqualTo(other)
    }

    override fun toString(): String {
        val textOutputBuilder = TextOutputBuilder()
        SimpleTreeType.write(this, textOutputBuilder)
        return textOutputBuilder.toString()
    }
}

/**
 * A node with a simple value (string, number, boolean, null) represented by a JsonTokenData object.
 */
class SimpleNode(
    val token: JsonTokenData,
    line: Int = -1,
    column: Int = -1
) : TreeNode(line, column) {

    override fun isEqualTo(other: TreeNode): Boolean {
        return other is SimpleNode && token == other.token
    }

    override fun hashCode(): Int = token.hashCode()
}

class ArrayNode(
    line: Int = -1,
    column: Int = -1,
    val nodes: MutableList<TreeNode> = mutableListOf()
) : TreeNode(line, column) {

    override fun isEqualTo(other: TreeNode): Boolean {
        return other is ArrayNode && nodes == other.nodes
    }

    override fun hashCode(): Int = nodes.hashCode()
}

class ObjectNode(
    line: Int = -1,
    column: Int = -1,
    val nodes: MutableMap<String, TreeNode> = mutableMapOf()
) : TreeNode(line, column) {
    override fun isEqualTo(other: TreeNode): Boolean {
        return other is ObjectNode && nodes == other.nodes
    }

    override fun textProperty(fieldName: String): String? {
        val child = nodes[fieldName]
        return if (child is SimpleNode && child.token.type == JsonTokenType.VALUE_TEXT) {
            child.token.text!!
        } else {
            null
        }
    }

    override fun hashCode(): Int = nodes.hashCode()
}