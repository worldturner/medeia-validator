package com.worldturner.medeia.schema.validation.stream

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.SchemaValidator

interface SchemaValidatorInstance {
    fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult?
}

class SchemaValidatingConsumer(val validator: SchemaValidator, val startLevel: Int = 0) :
    JsonTokenDataAndLocationBuilder {
    private var result: ValidationResult? = null
    private var instance: SchemaValidatorInstance? = null

    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        if (location.level == startLevel && token.type.firstToken) {
            // First token at start level: reset validation for .jsonl data
            result = null
            instance = validator.createInstance(startLevel)
        }
        if (result == null) {
            instance!!.validate(token, location)
                .also { this.result = it }
                ?.let { result ->
                    if (result is FailedValidationResult)
                        throw ValidationFailedException(result)
                }
        }
    }

    override fun takeResult(): Any? = result
}
