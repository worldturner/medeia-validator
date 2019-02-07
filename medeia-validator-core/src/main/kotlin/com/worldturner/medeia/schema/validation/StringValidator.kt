package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class StringValidator private constructor(
    val maxLength: Int?,
    val minLength: Int?,
    val pattern: Regex?
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type != VALUE_TEXT) {
            return OkValidationResult
        }
        return validateText(token.text!!, location)
    }

    private fun validateText(string: String, location: JsonTokenLocation): ValidationResult {
        val codePoints = string.codePointCount(0, string.length)
        maxLength?.let {
            if (codePoints > it) {
                return FailedValidationResult(
                    location = location,
                    failedRule = "maxLength",
                    message = "String length $codePoints is greater than maxLength $it"
                )
            }
        }
        minLength?.let {
            if (codePoints < it) {
                return FailedValidationResult(
                    location = location,
                    failedRule = "minLength",
                    message = "String length $codePoints is smaller than maxLength $it"
                )
            }
        }
        pattern?.let {
            if (!it.containsMatchIn(string)) {
                return FailedValidationResult(
                    location = location,
                    failedRule = "pattern",
                    message = "Pattern $it is not contained in text"
                )
            }
        }

        return OkValidationResult
    }

    companion object {
        fun create(maxLength: Int?, minLength: Int?, pattern: Regex?): StringValidator? =
            if (isAnyNotNull(maxLength, minLength, pattern))
                StringValidator(maxLength, minLength, pattern)
            else
                null
    }
}