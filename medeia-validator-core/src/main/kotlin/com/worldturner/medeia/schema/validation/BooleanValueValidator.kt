package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class BooleanValueValidator(
    val booleanValue: Boolean
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        BooleanValidatorInstance(booleanValue, startLevel)

    companion object {
        fun create(booleanValue: Boolean?): BooleanValueValidator? =
            booleanValue?.let { BooleanValueValidator(it) }
    }
}

class BooleanValidatorInstance(val booleanValue: Boolean, val startLevel: Int) :
    SchemaValidatorInstance {
    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? =
        if (token.type.lastToken && location.level == startLevel)
            if (booleanValue)
                OkValidationResult
            else
                FailedValidationResult(
                    location = location,
                    rule = "false",
                    message = "Nothing is valid according to this schema"
                )
        else
            null
}