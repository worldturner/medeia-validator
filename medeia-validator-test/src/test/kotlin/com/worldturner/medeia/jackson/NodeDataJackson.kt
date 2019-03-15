package com.worldturner.medeia.jackson

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DecimalNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.SimpleNode

fun TreeNode.toTreeNode(): JsonNode =
    when (this) {
        is SimpleNode -> {
            when (token.type) {
                JsonTokenType.VALUE_NUMBER -> DecimalNode(token.toDecimal())
                JsonTokenType.VALUE_TEXT -> TextNode(token.text)
                JsonTokenType.VALUE_BOOLEAN_FALSE -> BooleanNode.getFalse()
                JsonTokenType.VALUE_BOOLEAN_TRUE -> BooleanNode.getTrue()
                JsonTokenType.VALUE_NULL -> NullNode.getInstance()
                else -> throw IllegalStateException()
            }
        }
        is com.worldturner.medeia.parser.ArrayNode -> {
            ArrayNode(JsonNodeFactory.instance, nodes.map { it.toTreeNode() })
        }
        is com.worldturner.medeia.parser.ObjectNode -> {
            ObjectNode(JsonNodeFactory.instance, nodes.mapValues { it.value.toTreeNode() })
        }
        else -> {
            throw IllegalStateException()
        }
    }
