package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI

class MultiNumber(val bigDecimalValue: BigDecimal) {
    val longValue = try {
        bigDecimalValue.longValueExact()
    } catch (e: ArithmeticException) {
        null
    }
    val bigIntegerValue = try {
        bigDecimalValue.toBigIntegerExact()
    } catch (e: ArithmeticException) {
        null
    }

    fun isMultiple(token: JsonTokenData): Boolean {
        if (token.hasLongValue() && longValue != null) {
            return token.longValue % longValue == 0L
        }
        if (token.integer != null && bigIntegerValue != null) {
            return token.integer.rem(bigIntegerValue) == BigInteger.ZERO
        }
        return token.toDecimal().rem(bigDecimalValue).compareTo(BigDecimal.ZERO) == 0
    }

    operator fun compareTo(token: JsonTokenData): Int {
        if (token.hasLongValue() && longValue != null) {
            return longValue.compareTo(token.longValue)
        }
        if (token.integer != null && bigIntegerValue != null) {
            return bigIntegerValue.compareTo(token.integer)
        }
        return bigDecimalValue.compareTo(token.toDecimal())
    }

    override fun toString(): String = bigDecimalValue.toString()
}

class NumberValidator(
    multipleOf: BigDecimal?,
    maximum: BigDecimal?,
    exclusiveMaximum: BigDecimal?,
    minimum: BigDecimal?,
    exclusiveMinimum: BigDecimal?
) : SchemaValidator, SchemaValidatorInstance {

    val multipleOf = multipleOf?.let { MultiNumber(it) }
    val maximum = maximum?.let { MultiNumber(it) }
    val exclusiveMaximum = exclusiveMaximum?.let { MultiNumber(it) }
    val minimum = minimum?.let { MultiNumber(it) }
    val exclusiveMinimum = exclusiveMinimum?.let { MultiNumber(it) }

    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun recordUnknownRefs(unknownRefs: MutableCollection<URI>) = Unit

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        return if (token.type != JsonTokenType.VALUE_NUMBER)
            OkValidationResult
        else
            return validateNumber(token, location)
    }

    private fun validateNumber(token: JsonTokenData, location: JsonTokenLocation): ValidationResult {
        multipleOf?.let {
            if (!multipleOf.isMultiple(token)) {
                return FailedValidationResult(
                    location = location,
                    rule = "multipleOf",
                    message = "Value $token is not a multiple of $multipleOf"
                )
            }
        }
        maximum?.let {
            if (maximum < token) {
                return FailedValidationResult(
                    location = location,
                    rule = "maximum",
                    message = "Value $token is greater than maximum $maximum"
                )
            }
        }
        exclusiveMaximum?.let {
            if (exclusiveMaximum <= token) {
                return FailedValidationResult(
                    location = location,
                    rule = "exclusiveMaximum",
                    message = "Value $token is greater than or equal to exclusive maximum $exclusiveMaximum"
                )
            }
        }
        minimum?.let {
            if (minimum > token) {
                return FailedValidationResult(
                    location = location,
                    rule = "minimum",
                    message = "Value $token is smaller than minimum $minimum"
                )
            }
        }
        exclusiveMinimum?.let {
            if (exclusiveMinimum >= token) {
                return FailedValidationResult(
                    location = location,
                    rule = "exclusiveMinimum",
                    message = "Value $token is smaller than or equal to exclusive minimum $exclusiveMinimum"
                )
            }
        }
        return OkValidationResult
    }

    companion object {
        fun create(
            multipleOf: BigDecimal?,
            maximum: BigDecimal?,
            exclusiveMaximum: BigDecimal?,
            minimum: BigDecimal?,
            exclusiveMinimum: BigDecimal?
        ): NumberValidator? =
            if (isAnyNotNull(multipleOf, maximum, exclusiveMaximum, minimum, exclusiveMinimum))
                NumberValidator(
                    multipleOf = multipleOf,
                    maximum = maximum,
                    exclusiveMaximum = exclusiveMaximum,
                    minimum = minimum,
                    exclusiveMinimum = exclusiveMinimum
                )
            else
                null
    }
}
