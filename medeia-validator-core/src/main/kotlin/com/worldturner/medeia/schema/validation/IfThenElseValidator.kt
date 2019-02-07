package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class IfThenElseValidator(
    val ifValidator: SchemaValidator,
    val thenValidator: SchemaValidator?,
    val elseValidator: SchemaValidator?
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        IfThenElseValidatorInstance(
            startLevel,
            ifValidator.createInstance(startLevel),
            thenValidator?.createInstance(startLevel),
            elseValidator?.createInstance(startLevel)
        )

    companion object {
        fun create(
            ifValidator: SchemaValidator?,
            thenValidator: SchemaValidator?,
            elseValidator: SchemaValidator?
        ) =
            ifValidator?.let {
                IfThenElseValidator(ifValidator, thenValidator, elseValidator)
            }
    }
}

class IfThenElseValidatorInstance(
    val startLevel: Int,
    val ifValidator: SchemaValidatorInstance,
    val thenValidator: SchemaValidatorInstance?,
    val elseValidator: SchemaValidatorInstance?
) : SchemaValidatorInstance {

    var ifResult: ValidationResult? = null
    var thenResult: ValidationResult? = null
    var elseResult: ValidationResult? = null

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (ifResult == null) {
            ifResult = ifValidator.validate(token, location)
        }
        if (thenResult == null && thenValidator != null) {
            thenResult = thenValidator.validate(token, location)
        }
        if (elseResult == null && elseValidator != null) {
            elseResult = elseValidator.validate(token, location)
        }
        if (location.level == startLevel && token.type.lastToken) {
            ifResult?.let { ifResult ->
                if (ifResult.valid) {
                    return if (thenResult != null) thenResult else OkValidationResult
                } else {
                    return if (elseResult != null) elseResult else OkValidationResult
                }
            }
        }
        return null
    }
}
