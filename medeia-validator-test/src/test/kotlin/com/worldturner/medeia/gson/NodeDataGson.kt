package com.worldturner.medeia.gson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.worldturner.medeia.parser.ArrayNode
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.ObjectNode
import com.worldturner.medeia.parser.SimpleNode

fun TreeNode.toJsonElement(): JsonElement =
    when (this) {
        is SimpleNode -> {
            when (token.type) {
                JsonTokenType.VALUE_NUMBER -> JsonPrimitive(token.toDecimal())
                JsonTokenType.VALUE_TEXT -> JsonPrimitive(token.text)
                JsonTokenType.VALUE_BOOLEAN_FALSE -> JsonPrimitive(false)
                JsonTokenType.VALUE_BOOLEAN_TRUE -> JsonPrimitive(true)
                JsonTokenType.VALUE_NULL -> JsonNull.INSTANCE
                else -> throw IllegalStateException()
            }
        }
        is ArrayNode ->
            JsonArray().also { array ->
                nodes.forEach { array.add(it.toJsonElement()) }
            }
        is ObjectNode -> JsonObject().also { obj ->
            nodes.forEach { obj.add(it.key, it.value.toJsonElement()) }
        }
        else -> {
            throw IllegalStateException()
        }
    }
