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
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.format.DateTimeParseException
import java.util.regex.PatternSyntaxException

val formatRegexes = emptyMap<String, Regex>()

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
            "uri-reference", "uri", "iri-reference", "iri" -> try {
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
                        message = "Invalid json-pointer syntax: ${e.message}"
                    )
                }
            "date" ->
                try {
                    LocalDate.parse(string).let { OkValidationResult }
                } catch (e: DateTimeParseException) {
                    FailedValidationResult(
                        location = location,
                        rule = "format",
                        message = "Invalid time: ${e.message}"
                    )
                }
            "time" ->
                try {
                    OffsetTime.parse(string).let { OkValidationResult }
                } catch (e: DateTimeParseException) {
                    FailedValidationResult(
                        location = location,
                        rule = "format",
                        message = "Invalid time: ${e.message}"
                    )
                }
            "date-time" ->
                try {
                    OffsetDateTime.parse(string).let { OkValidationResult }
                } catch (e: DateTimeParseException) {
                    FailedValidationResult(
                        location = location,
                        rule = "format",
                        message = "Invalid date-time: ${e.message}"
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