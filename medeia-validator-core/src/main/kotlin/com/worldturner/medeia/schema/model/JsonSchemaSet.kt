package com.worldturner.medeia.schema.model

import com.worldturner.medeia.schema.validation.SchemaValidator
import java.net.URI

data class JsonSchemaSet(
    val main: Schema,
    val additional: Set<Schema>
) : Schema {

    override fun buildValidator(context: ValidationBuilderContext): SchemaValidator {
        val mainValidator = main.buildValidator(context)
        additional.map { it.buildValidator(context) }
        return mainValidator
    }

    override var resolvedId: URI? = null
}