package com.worldturner.medeia.gson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.worldturner.medeia.parser.ArrayNodeData
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.ObjectNodeData
import com.worldturner.medeia.parser.TokenNodeData

fun NodeData.toJsonElement(): JsonElement =
    when (this) {
        is TokenNodeData -> {
            when (token.type) {
                JsonTokenType.VALUE_NUMBER -> JsonPrimitive(token.toDecimal())
                JsonTokenType.VALUE_TEXT -> JsonPrimitive(token.text)
                JsonTokenType.VALUE_BOOLEAN_FALSE -> JsonPrimitive(false)
                JsonTokenType.VALUE_BOOLEAN_TRUE -> JsonPrimitive(true)
                JsonTokenType.VALUE_NULL -> JsonNull.INSTANCE
                else -> throw IllegalStateException()
            }
        }
        is ArrayNodeData ->
            JsonArray().also { array ->
                nodes.forEach { array.add(it.toJsonElement()) }
            }
        is ObjectNodeData -> JsonObject().also { obj ->
            nodes.forEach { obj.add(it.key, it.value.toJsonElement()) }
        }
        else -> {
            throw IllegalStateException()
        }
    }
