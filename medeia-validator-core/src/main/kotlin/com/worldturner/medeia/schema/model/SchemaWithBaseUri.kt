package com.worldturner.medeia.schema.model

import com.worldturner.medeia.schema.validation.SchemaValidator
import java.net.URI

class SchemaWithBaseUri(
    val baseUri: URI,
    val schema: Schema
) : Schema {
    override fun buildValidator(context: ValidationBuilderContext): SchemaValidator {
        val subContext = context.withBaseUri(baseUri, root = true)
        return schema.buildValidator(subContext)
    }

    override var resolvedId: URI? = baseUri
}