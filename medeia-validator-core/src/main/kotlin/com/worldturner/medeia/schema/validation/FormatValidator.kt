package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.net.URI
import java.util.regex.PatternSyntaxException

class FormatValidator(
    val format: String
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type != VALUE_TEXT) {
            return OkValidationResult
        }
        val string = token.text!!
        when (format) {
            "regex" -> try {
                Regex(string)
            } catch (e: PatternSyntaxException) {
                return FailedValidationResult(
                    location = location,
                    failedRule = "format",
                    message = "Invalid regex syntax"
                )
            }
            "uri-reference" -> try {
                URI.create(string)
            } catch (e: IllegalArgumentException) {
                return FailedValidationResult(
                    location = location,
                    failedRule = "format",
                    message = "Invalid regex syntax"
                )
            }
        }
        return OkValidationResult
    }

    companion object {
        fun create(format: String?): FormatValidator? =
            format?.let { FormatValidator(format) }
    }
}