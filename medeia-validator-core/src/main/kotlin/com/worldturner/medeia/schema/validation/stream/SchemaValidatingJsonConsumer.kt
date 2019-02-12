package com.worldturner.medeia.schema.validation.stream

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.JsonTokenLocation

interface SchemaValidatorInstance {
    fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult?
}

class SchemaValidatingConsumer(val validator: SchemaValidatorInstance) : JsonTokenDataAndLocationBuilder {
    private var result: ValidationResult? = null
    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        if (result == null) {
            val result = validator.validate(token, location)
            this.result = result
            if (result != null && !result.valid)
                throw ValidationFailedException(result as FailedValidationResult)
        }
    }

    override fun takeResult(): Any? = result
}
