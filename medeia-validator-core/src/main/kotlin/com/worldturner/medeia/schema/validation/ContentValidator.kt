package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class ContentValidator(
    val contentMediaType: String?,
    val contentEncoding: String?
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type != VALUE_TEXT) {
            return OkValidationResult
        }
        return validateText(token.text!!)
    }

    private fun validateText(string: String): ValidationResult {
        return OkValidationResult
    }

    companion object {
        fun create(contentMediaType: String?, contentEncoding: String?): ContentValidator? =
            if (isAnyNotNull(contentMediaType, contentEncoding))
                ContentValidator(contentMediaType, contentEncoding)
            else
                null
    }
}