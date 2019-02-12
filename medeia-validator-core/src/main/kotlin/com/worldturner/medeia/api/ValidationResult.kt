package com.worldturner.medeia.api

import com.worldturner.medeia.parser.JsonTokenLocation

sealed class ValidationResult(val valid: Boolean)

object OkValidationResult : ValidationResult(true)

data class FailedValidationResult(
    val failedRule: String = "",
    val failedProperty: String? = null,
    val message: String,
    val location: String,
    val subResults: Collection<ValidationResult> = emptySet()
) : ValidationResult(false) {

    constructor(
        failedRule: String = "",
        failedProperty: String? = null,
        message: String,
        location: JsonTokenLocation,
        subResults: Set<ValidationResult> = emptySet()
    ) : this(failedRule, failedProperty, message, location.toString(), subResults)
}
