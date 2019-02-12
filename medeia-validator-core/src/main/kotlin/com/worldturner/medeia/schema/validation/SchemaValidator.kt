package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.net.URI

// https://json-schema.org/latest/json-schema-validation.html#rfc.section.10.2

fun isAnyNotNull(vararg args: Any?) = args.any { it != null }

interface SchemaValidator {
    fun createInstance(startLevel: Int = 0): SchemaValidatorInstance
}

class RefSchemaValidator(val ref: URI, val ids: Map<URI, SchemaValidator>) : SchemaValidator {

    var resolved: SchemaValidator? = null

    override fun createInstance(startLevel: Int): SchemaValidatorInstance {
        resolved?.let { return it.createInstance(startLevel) }
        resolved = ids[ref]
        return resolved?.let { it.createInstance(startLevel) } ?: PredeterminedSchemaValidatorInstance(
            failedRule = "\$ref",
            message = "Cannot find \$ref $ref"
        )
    }
}

class PredeterminedSchemaValidatorInstance(
    val failedRule: String,
    val message: String
) : SchemaValidatorInstance {
    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        return FailedValidationResult(
            location = location,
            failedRule = failedRule,
            message = message
        )
    }
}