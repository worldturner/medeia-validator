package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.ExistentialOperation.ALL_OF
import com.worldturner.medeia.schema.validation.ExistentialOperation.ANY_OF
import com.worldturner.medeia.schema.validation.ExistentialOperation.ONE_OF
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

enum class ExistentialOperation {
    ALL_OF,
    ONE_OF,
    ANY_OF
}

class ExistentialValidator(
    val operation: ExistentialOperation,
    val validations: List<SchemaValidator>
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        ExistentialValidatorInstance(operation, startLevel, validations)

    companion object {
        fun create(operation: ExistentialOperation, validations: List<SchemaValidator>?, optimize: Boolean) =
            validations?.let {
                if (optimize) {
                    if (operation == ALL_OF)
                        AllOfValidator(validations)
                    else if (operation == ANY_OF)
                        AnyOfValidator(validations)
                    else
                        ExistentialValidator(operation, validations)
                } else {
                    ExistentialValidator(operation, validations)
                }
            }
    }
}

/** Optimized ExistentialValidator */
class AllOfValidator(
    val validations: List<SchemaValidator>
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        AllOfValidatorInstance(startLevel, validations)
}

/** Optimized ExistentialValidator */
class AnyOfValidator(
    val validations: List<SchemaValidator>
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        AnyOfValidatorInstance(startLevel, validations)
}

class ExistentialValidatorInstance(
    val operation: ExistentialOperation,
    val startLevel: Int,
    validations: List<SchemaValidator>
) : SchemaValidatorInstance {

    private val instances =
        validations.mapTo(ArrayList<SchemaValidatorInstance>(validations.size)) { it.createInstance(startLevel) }
    private val results = ArrayList<ValidationResult>(instances.size)

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        processValidators(token, location)

        if (location.level == startLevel && token.type.lastToken) {
            return finalStep(location)
        } else {
            return null
        }
    }

    private fun finalStep(location: JsonTokenLocation): ValidationResult? {
        return when (operation) {
            ALL_OF ->
                if (results.all { v -> v.valid })
                    OkValidationResult
                else
                    FailedValidationResult(
                        location = location,
                        failedRule = "allOf",
                        message = "Some of the allOf validations failed",
                        subResults = results.filter { !it.valid }.toSet()
                    )
            ANY_OF ->
                if (results.any { v -> v.valid })
                    OkValidationResult
                else
                    FailedValidationResult(
                        location = location,
                        failedRule = "anyOf",
                        message = "None of the anyOf validations succeeded",
                        subResults = results.toSet()
                    )
            ONE_OF ->
                if (results.count { v -> v.valid } == 1)
                    OkValidationResult
                else
                    FailedValidationResult(
                        location = location,
                        failedRule = "oneOf",
                        message = "More or less than one of the oneOf validations succceeded",
                        subResults = results.toSet()
                    )
        }
    }

    private fun processValidators(token: JsonTokenData, location: JsonTokenLocation) {
        var size = instances.size
        var i = 0
        while (i < size) {
            val instance = instances[i]
            val result = instance.validate(token, location)
            result?.let {
                results += result
                instances.removeAt(i)
                size--
            } ?: i++
        }
    }
}

/**
 * Optimized ExistentialValidatorInstance
 */
class AllOfValidatorInstance(
    val startLevel: Int,
    validations: List<SchemaValidator>
) : SchemaValidatorInstance {

    private val instances =
        validations.mapTo(ArrayList<SchemaValidatorInstance>(validations.size)) { it.createInstance(startLevel) }

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        processValidators(token, location)?.let { return it }

        if (location.level == startLevel && token.type.lastToken) {
            return OkValidationResult
        } else {
            return null
        }
    }

    private fun processValidators(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        var size = instances.size
        var i = 0
        while (i < size) {
            val instance = instances[i]
            val result = instance.validate(token, location)
            result?.let {
                if (!result.valid)
                    return result
                instances.removeAt(i)
                size--
            } ?: i++
        }
        return null
    }
}

/**
 * Optimized ExistentialValidatorInstance
 */
class AnyOfValidatorInstance(
    val startLevel: Int,
    validations: List<SchemaValidator>
) : SchemaValidatorInstance {

    private val instances =
        validations.mapTo(ArrayList<SchemaValidatorInstance>(validations.size)) { it.createInstance(startLevel) }

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        processValidators(token, location)?.let { return it }

        if (location.level == startLevel && token.type.lastToken) {
            return FailedValidationResult(
                location = location,
                failedRule = "anyOf",
                message = "None of the anyOf validations succeeded"
            )
        } else {
            return null
        }
    }

    private fun processValidators(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        var size = instances.size
        var i = 0
        while (i < size) {
            val instance = instances[i]
            val result = instance.validate(token, location)
            result?.let {
                if (result.valid)
                    return result
                instances.removeAt(i)
                size--
            } ?: i++
        }
        return null
    }
}
