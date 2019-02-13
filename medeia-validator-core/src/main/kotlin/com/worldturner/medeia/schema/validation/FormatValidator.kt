package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.net.URI
import java.util.regex.PatternSyntaxException

val formatRegexes = mapOf(
    "date" to Regex("""\d{4}-\d{2}-\d{2}""")
)

class FormatValidator(
    val format: String
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type != VALUE_TEXT) {
            return OkValidationResult
        }
        val string = token.text!!
        return if (format in formatRegexes) {
            if (formatRegexes[format]?.matchEntire(string) != null)
                OkValidationResult
            else
                FailedValidationResult(
                    location = location,
                    rule = "format",
                    message = "Data does not match format '$format'"
                )
        } else when (format) {
            "regex" -> try {
                Regex(string).let { OkValidationResult }
            } catch (e: PatternSyntaxException) {
                FailedValidationResult(
                    location = location,
                    rule = "format",
                    message = "Invalid regex syntax"
                )
            }
            "uri-reference" -> try {
                URI.create(string).let { OkValidationResult }
            } catch (e: IllegalArgumentException) {
                FailedValidationResult(
                    location = location,
                    rule = "format",
                    message = "Invalid regex syntax"
                )
            }
            "json-pointer" ->
                try {
                    JsonPointer(string).let { OkValidationResult }
                } catch (e: IllegalArgumentException) {
                    FailedValidationResult(
                        location = location,
                        rule = "format",
                        message = "Invalid json-pointer syntax"
                    )
                }

            else -> OkValidationResult
        }
    }

    companion object {
        fun create(format: String?): FormatValidator? =
            format?.let { FormatValidator(format) }
    }
}