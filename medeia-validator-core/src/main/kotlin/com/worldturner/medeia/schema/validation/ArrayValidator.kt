package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.END_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance

class ArrayValidator(
    val additionalItems: SchemaValidator?,
    val items: List<SchemaValidator>?,
    val allItems: SchemaValidator?,
    val maxItems: Int?,
    val minItems: Int?,
    val contains: SchemaValidator?
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        ArrayValidatorInstance(this, startLevel)

    companion object {
        fun create(
            additionalItems: SchemaValidator?,
            items: List<SchemaValidator>?,
            allItems: SchemaValidator?,
            maxItems: Int?,
            minItems: Int?,
            contains: SchemaValidator?
        ): ArrayValidator? =
            if (isAnyNotNull(additionalItems, items, allItems, maxItems, minItems, contains))
                ArrayValidator(additionalItems, items, allItems, maxItems, minItems, contains)
            else null
    }
}

class ArrayValidatorInstance(val validator: ArrayValidator, val startLevel: Int) :
    SchemaValidatorInstance {
    private var first = true
    private var itemCount = 0
    private var containsMatched = false
    private var allItemsInstance: SchemaValidatorInstance? = null
    private var containsInstance: SchemaValidatorInstance? = null
    private var currentItemInstance: SchemaValidatorInstance? = null

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (first && token.type != START_ARRAY) {
            return OkValidationResult
        }
        first = false
        if (location.level == startLevel + 1 && token.type.firstToken) {

            validator.contains?.let {
                containsInstance = it.createInstance(location.level)
            }
            if (validator.allItems != null) {
                allItemsInstance = validator.allItems.createInstance(location.level)
            } else if (validator.items != null) {
                if (itemCount < validator.items.size) {
                    currentItemInstance = validator.items[itemCount].createInstance(location.level)
                } else if (validator.additionalItems != null) {
                    currentItemInstance = validator.additionalItems.createInstance(location.level)
                }
            }
        }

        allItemsInstance?.let {
            val result = it.validate(token, location)
            if (result != null) {
                if (result is FailedValidationResult)
                    return FailedValidationResult(
                        rule = "items",
                        message = "Schema for all items failed to validate",
                        location = location,
                        details = setOf(result)
                    )
                allItemsInstance = null
            }
        }
        containsInstance?.let {
            val result = it.validate(token, location)
            if (result != null) {
                if (result.valid)
                    containsMatched = true
                containsInstance = null
            }
        }
        currentItemInstance?.let {
            val result = it.validate(token, location)
            if (result != null) {
                if (result is FailedValidationResult)
                    return FailedValidationResult(
                        location = location,
                        rule = "items/additionalItems",
                        message = "Schema for items failed to validate",
                        details = setOf(result)
                    )
                currentItemInstance = null
            }
        }
        if (token.type.lastToken && location.level == startLevel + 1) {
            itemCount++
        }

        if (token.type == END_ARRAY && location.level == startLevel)
            return completeValidation(location)
        else
            return null
    }

    private fun completeValidation(location: JsonTokenLocation): ValidationResult {
        validator.contains?.let {
            if (!containsMatched) {
                return FailedValidationResult(
                    location = location,
                    rule = "contains",
                    message = "Items don't contain an item that matches the 'contains' schemaValue"
                )
            }
        }
        validator.maxItems?.let {
            if (itemCount > it) {
                return FailedValidationResult(
                    location = location,
                    rule = "maxItems",
                    message = "Value $itemCount is greater than maxItems $it"
                )
            }
        }
        validator.minItems?.let {
            if (itemCount < it) {
                return FailedValidationResult(
                    location = location,
                    rule = "minItems",
                    message = "Value $itemCount is smaller than minItems $it"
                )
            }
        }
        return OkValidationResult
    }
}