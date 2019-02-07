package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.parser.JsonTokenLocation
import java.net.URI

abstract class ValidationResult(val valid: Boolean)

object OkValidationResult : ValidationResult(true)

data class FailedValidationResult(
    val failedRule: String = "",
    val failedProperty: String = "",
    val message: String,
    val location: String,
    val schemaUri: URI? = null,
    val subResults: Set<ValidationResult> = emptySet()
) : ValidationResult(false) {

    constructor(
        failedRule: String = "",
        failedProperty: String = "",
        message: String,
        location: JsonTokenLocation,
        schemaUri: URI? = null,
        subResults: Set<ValidationResult> = emptySet()
    ) :
        this(failedRule, failedProperty, message, location.toString(), schemaUri, subResults)
}
