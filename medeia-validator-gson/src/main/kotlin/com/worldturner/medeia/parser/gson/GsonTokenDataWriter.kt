package com.worldturner.medeia.parser.gson

import com.google.gson.stream.JsonWriter
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_FALSE
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.TOKEN_TRUE
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.pointer.JsonPointerBuilder
import java.io.Writer
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayDeque
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class GsonTokenDataWriter(
    output: Writer,
    val consumer: JsonTokenDataAndLocationConsumer
) : JsonWriter(output) {

    var level: Int = 0
        private set
    private val jsonPointerBuilder = JsonPointerBuilder()
    private val dynamicTokenLocation = DynamicJsonTokenLocation()
    private val propertyNamesStack = ArrayDeque<MutableSet<String>>()

    inner class DynamicJsonTokenLocation : JsonTokenLocation {
        override val level: Int
            get() = this@GsonTokenDataWriter.level
        override val pointer: JsonPointer
            get() = jsonPointerBuilder.toJsonPointer()
        override val propertyNames: Set<String>
            get() = propertyNamesStack.peek() ?: emptySet()

        override fun toString(): String = "at $pointer"
    }

    private fun consume(token: JsonTokenData) {
        jsonPointerBuilder.consume(token)
        consumer.consume(token, dynamicTokenLocation)
    }

    override fun jsonValue(value: String?): JsonWriter {
        throw UnsupportedOperationException()
    }

    override fun nullValue(): JsonWriter {
        consume(TOKEN_NULL)
        return super.nullValue()
    }

    override fun value(value: Boolean): JsonWriter {
        consume(if (value) TOKEN_TRUE else TOKEN_FALSE)
        return super.value(value)
    }

    override fun value(value: String): JsonWriter {
        consume(JsonTokenData.createText(value))
        return super.value(value)
    }

    override fun name(name: String): JsonWriter {
        consume(JsonTokenData(JsonTokenType.FIELD_NAME, text = name))
        propertyNamesStack.peek() += name
        return super.name(name)
    }

    override fun value(value: Double): JsonWriter {
        val token = JsonTokenData(
            JsonTokenType.VALUE_NUMBER,
            decimal = BigDecimal.valueOf(value)
        )
        consume(token)
        return super.value(value)
    }

    override fun value(value: Long): JsonWriter {
        consume(JsonTokenData.createNumber(value))
        return super.value(value)
    }

    override fun value(value: Number): JsonWriter {
        val token = when (value) {
            is BigInteger -> JsonTokenData(
                JsonTokenType.VALUE_NUMBER,
                integer = value
            )
            is BigDecimal -> JsonTokenData(
                JsonTokenType.VALUE_NUMBER,
                decimal = value
            )
            is Int, is AtomicInteger, is Short, is Byte ->
                JsonTokenData.createNumber(value.toLong())
            is Long, is AtomicLong ->
                JsonTokenData.createNumber(value.toLong())
            else -> JsonTokenData(
                JsonTokenType.VALUE_NUMBER,
                decimal = BigDecimal.valueOf(value.toDouble())
            )
        }
        consume(token)
        return super.value(value)
    }

    override fun beginArray(): JsonWriter {
        consume(TOKEN_START_ARRAY)
        level++
        super.beginArray()
        return this
    }

    override fun beginObject(): JsonWriter {
        consume(TOKEN_START_OBJECT)
        propertyNamesStack.addFirst(HashSet())
        level++
        super.beginObject()
        return this
    }

    override fun endArray(): JsonWriter {
        level--
        consume(TOKEN_END_ARRAY)
        return super.endArray()
    }

    override fun endObject(): JsonWriter {
        level--
        consume(TOKEN_END_OBJECT)
        propertyNamesStack.removeFirst()
        return super.endObject()
    }
}