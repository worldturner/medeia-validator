package com.worldturner.medeia.parser

import com.worldturner.medeia.schema.validation.HashResult
import com.worldturner.medeia.schema.validation.NodeHasher
import com.worldturner.medeia.schema.validation.NodeHasher.Companion.TYPE_TOKEN
import com.worldturner.util.appendJsonString
import com.worldturner.util.updateValue
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest

val TOKEN_TRUE = JsonTokenData(JsonTokenType.VALUE_BOOLEAN_TRUE)
val TOKEN_FALSE = JsonTokenData(JsonTokenType.VALUE_BOOLEAN_FALSE)
val TOKEN_NULL = JsonTokenData(JsonTokenType.VALUE_NULL)
val TOKEN_START_ARRAY = JsonTokenData(JsonTokenType.START_ARRAY)
val TOKEN_END_ARRAY = JsonTokenData(JsonTokenType.END_ARRAY)
val TOKEN_START_OBJECT = JsonTokenData(JsonTokenType.START_OBJECT)
val TOKEN_END_OBJECT = JsonTokenData(JsonTokenType.END_OBJECT)
val TOKEN_NONE = JsonTokenData(JsonTokenType.NONE)
val TOKEN_END_OF_STREAM = JsonTokenData(JsonTokenType.END_OF_STREAM)
val TOKEN_EMPTY_STRING = JsonTokenData(JsonTokenType.VALUE_TEXT, text = "")

class JsonTokenData(
    val type: JsonTokenType,
    val text: String? = null,
    /** Node: MIN_VALUE cannot be stored in a primitive long and needs to go into a BigInteger. */
    val longValue: Long = Long.MIN_VALUE,
    val integer: BigInteger? = null,
    decimal: BigDecimal? = null
) : NodeHasher, HashResult {

    /**
     * Decimal is guaranteed to have trailing zeros stripped - this eases numerical comparisons and more.
     */
    val decimal = decimal?.let { it.stripTrailingZeros() }

    fun toBoolean() =
        when (type) {
            JsonTokenType.VALUE_BOOLEAN_TRUE -> true
            JsonTokenType.VALUE_BOOLEAN_FALSE -> false
            else -> throw IllegalStateException("$type")
        }

    fun hasLongValue() = longValue != Long.MIN_VALUE

    fun toDecimal() =
        if (hasLongValue()) BigDecimal.valueOf(longValue) else integer?.toBigDecimal() ?: decimal!!

    val value: Any?
        get() = text ?: integer ?: decimal

    override fun digest(digester: MessageDigest) {
        digester.update(TYPE_TOKEN)
        digester.update(type.ordinal.toByte())
        if (hasLongValue()) {
            digester.updateValue(longValue)
        }
        text?.let { digester.updateValue(text) }
        integer?.let { digester.updateValue(integer) }
        decimal?.let { digester.updateValue(decimal) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JsonTokenData

        if (type != other.type) return false

        return when (type) {
            JsonTokenType.VALUE_TEXT -> text == other.text
            JsonTokenType.VALUE_NUMBER -> {
                if (integer != null && other.integer != null)
                    integer == other.integer
                else if (decimal != null && other.decimal != null)
                    decimal.compareTo(other.decimal) == 0
                else
                    toDecimal() == other.toDecimal()
            }
            else -> true
        }
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + (text?.hashCode() ?: 0)
        result = 31 * result + (integer?.standardizedHashCode() ?: 0)
        result = 31 * result + (decimal?.standardizedHashCode() ?: 0)
        return result
    }

    fun isInteger(): Boolean =
        hasLongValue() || integer != null || decimal!!.isIntegerForStrippedTrailingZeroes()

    override fun toString(): String {
        return when (type) {
            JsonTokenType.START_ARRAY -> "["
            JsonTokenType.END_ARRAY -> "]"
            JsonTokenType.VALUE_NUMBER -> if (hasLongValue()) longValue.toString() else
                integer?.toString() ?: decimal?.toString()!!
            JsonTokenType.START_OBJECT -> "{"
            JsonTokenType.END_OBJECT -> "}"
            JsonTokenType.VALUE_TEXT -> StringBuilder(text!!.length + 2).appendJsonString(text!!).toString()
            JsonTokenType.VALUE_NULL -> "null"
            JsonTokenType.VALUE_BOOLEAN_TRUE -> "true"
            JsonTokenType.VALUE_BOOLEAN_FALSE -> "false"
            else -> "?"
        }
    }

    companion object {
        val minLongValue = -127
        val maxLongValue = 127

        val array: Array<JsonTokenData> = Array(maxLongValue - minLongValue, init = {
            JsonTokenData(JsonTokenType.VALUE_NUMBER, longValue = minLongValue + it.toLong())
        })

        fun createText(text: String): JsonTokenData =
            if (text.isEmpty())
                TOKEN_EMPTY_STRING
            else
                JsonTokenData(JsonTokenType.VALUE_TEXT, text = text)

        fun createNumber(longValue: Long): JsonTokenData =
            if (longValue >= minLongValue && longValue <= maxLongValue) {
                array[longValue.toInt() - minLongValue]
            } else if (longValue == Long.MIN_VALUE) {
                JsonTokenData(JsonTokenType.VALUE_NUMBER, integer = BigInteger.valueOf(longValue))
            } else {
                JsonTokenData(JsonTokenType.VALUE_NUMBER, longValue = longValue)
            }
    }
}

fun Number.standardizedHashCode() = this.toDouble().hashCode()

fun <T : Comparable<T>> compare(a: T?, b: T?): Int =
    when {
        a === null -> if (b === null) 0 else -1
        b === null -> 1
        else -> a.compareTo(b)
    }

/**
 * Only works for BigDecimals on which stripTailingZeroes has been called
 */
internal fun BigDecimal.isIntegerForStrippedTrailingZeroes() = this.scale() <= 0
