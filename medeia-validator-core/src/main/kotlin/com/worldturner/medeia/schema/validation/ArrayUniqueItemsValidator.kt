package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.END_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.SimpleTreeBuilder
import com.worldturner.medeia.api.UniqueItemsValidationMethod
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.net.URI

class ArrayUniqueItemsValidator : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        ArrayUniqueItemsValidatorInstance(startLevel)

    override fun recordUnknownRefs(unknownRefs: MutableCollection<URI>) = Unit

    companion object {
        fun create(
            uniqueItems: Boolean?,
            method: UniqueItemsValidationMethod
        ): SchemaValidator? =
            if (uniqueItems == true) {
                if (method.digest)
                    ArrayUniqueItemsDigestValidator(digestAlgorithm = method.algorithm)
                else
                    ArrayUniqueItemsValidator()
            } else {
                null
            }
    }
}

class ArrayUniqueItemsValidatorInstance(val startLevel: Int) : SchemaValidatorInstance {
    private val uniqueItems: MutableSet<TreeNode> = mutableSetOf()
    private val treeBuilder: SimpleTreeBuilder = SimpleTreeBuilder(startLevel + 1)

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (location.level == startLevel && token.type != START_ARRAY) {
            return OkValidationResult
        }
        if (location.level > startLevel) {
            treeBuilder.consume(token, location)
            treeBuilder.takeResult()?.let { tree ->
                if (tree in uniqueItems) {
                    return FailedValidationResult(
                        location = location,
                        rule = "uniqueItems",
                        message = "Duplicate item"
                    )
                } else {
                    uniqueItems.add(tree)
                }
            }
        }
        if (token.type == END_ARRAY && location.level == startLevel)
            return OkValidationResult
        else
            return null
    }
}
