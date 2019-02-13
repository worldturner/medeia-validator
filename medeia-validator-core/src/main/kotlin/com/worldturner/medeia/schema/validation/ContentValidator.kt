package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import com.worldturner.util.JsonParseException
import com.worldturner.util.JsonValidator
import java.util.Base64

class DecodingResult(
    val array: ByteArray? = null,
    val string: String? = null,
    val failure: FailedValidationResult? = null
) {
    override fun toString(): String {
        string?.let { return it }
        array?.let { return String(it, Charsets.UTF_8) }
        throw IllegalStateException()
    }
}

class ContentValidator(
    val contentMediaType: String?,
    val contentEncoding: String?
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type != VALUE_TEXT) {
            return OkValidationResult
        }
        val text = token.text!!
        val decoding = validateContentEncoding(text, location).also { it.failure?.let { return it } }
        validateContentMediaType(decoding, location).let { return it }
        return null
    }

    private fun validateContentEncoding(
        string: String,
        location: JsonTokenLocation
    ): DecodingResult =
        when (contentEncoding) {
            "base64" -> {
                try {
                    Base64.getDecoder().decode(string).let { DecodingResult(array = it) }
                } catch (e: IllegalArgumentException) {
                    DecodingResult(
                        failure = FailedValidationResult(
                            rule = "contentEncoding",
                            message = "Invalid base64 data: ${e.message}",
                            location = location
                        )
                    )
                }
            }
            else -> DecodingResult(string = string)
        }

    private fun validateContentMediaType(decoding: DecodingResult, location: JsonTokenLocation): ValidationResult {
        return when (contentMediaType) {
            "application/json" -> {
                try {
                    JsonValidator(decoding.toString())
                    OkValidationResult
                } catch (e: JsonParseException) {
                    FailedValidationResult(
                        rule = "contentMediaType",
                        location = location,
                        message = "Invalid JSON: ${e.message}"
                    )
                }
            }
            else -> OkValidationResult
        }
    }

    companion object {
        fun create(contentMediaType: String?, contentEncoding: String?): ContentValidator? =
            if (isAnyNotNull(contentMediaType, contentEncoding))
                ContentValidator(contentMediaType, contentEncoding)
            else
                null
    }
}