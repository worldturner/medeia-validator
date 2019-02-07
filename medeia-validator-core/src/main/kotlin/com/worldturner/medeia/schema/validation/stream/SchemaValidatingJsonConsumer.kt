package com.worldturner.medeia.schema.validation.stream

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.ValidationResult

interface SchemaValidatorInstance {
    fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult?
}

class SchemaValidationFailedException(val validationResult: ValidationResult) :
    RuntimeException(validationResult.toString())

class SchemaValidatingConsumer(val validator: SchemaValidatorInstance) : JsonTokenDataAndLocationBuilder {
    private var result: ValidationResult? = null
    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        if (result == null) {
            val result = validator.validate(token, location)
            this.result = result
            if (result != null && !result.valid)
                throw SchemaValidationFailedException(result)
        }
    }

    override fun takeResult(): Any? = result
}
