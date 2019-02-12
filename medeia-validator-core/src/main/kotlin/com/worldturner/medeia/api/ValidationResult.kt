package com.worldturner.medeia.api

import com.worldturner.medeia.parser.JsonTokenLocation

sealed class ValidationResult(val valid: Boolean)

object OkValidationResult : ValidationResult(true)

data class FailedValidationResult(
    val rule: String,
    val property: String? = null,
    val message: String,
    val location: String,
    val details: Collection<FailedValidationResult> = emptySet()
) : ValidationResult(false) {

    constructor(
        rule: String,
        property: String? = null,
        message: String,
        location: JsonTokenLocation,
        details: Collection<FailedValidationResult> = emptySet()
    ) : this(rule, property, message, location.toString(), details)

    override fun toString(): String = toString(true)

    fun toString(outputHeading: Boolean): String {
        val b = StringBuilder()
        if (outputHeading) {
            b.append("Validation Failure\n")
            b.append("------------------\n")
        }
        b.append("Rule:     ").append(rule).append('\n')
        property?.let { b.append("Property: $property\n") }
        b.append("Message:  ").append(message).append('\n')
        b.append("Location: ").append(location).append('\n')
        if (details.isNotEmpty()) {
            b.append("Details:\n")
            details.forEach {
                b.append(it.toString(false).prependIndent("    "))
                b.append("-----\n")
            }
        }
        return b.toString()
    }
}
