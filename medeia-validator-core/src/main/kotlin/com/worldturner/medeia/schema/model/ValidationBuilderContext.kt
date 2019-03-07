package com.worldturner.medeia.schema.model

import com.worldturner.medeia.api.ValidationOptions
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.util.EMPTY_URI
import com.worldturner.util.resolveSafe
import java.net.URI

data class ValidationBuilderContext(
    val root: Boolean = true,
    val baseUri: URI = EMPTY_URI,
    val ids: MutableMap<URI, JsonSchema> = mutableMapOf(),
    val schemaValidatorsById: MutableMap<URI, SchemaValidator> = mutableMapOf(),
    val parents: List<JsonSchema> = emptyList(),
    val options: ValidationOptions = ValidationOptions.DEFAULT
) {
    fun withBaseUri(baseUri: URI, root: Boolean = false) =
        copy(root = root, baseUri = baseUri)

    fun withParent(parent: JsonSchema) =
        copy(parents = parents + parent)

    fun put(id: URI, schema: JsonSchema, validator: SchemaValidator) {
        if (id in ids) {
            if (id.toString().isNotEmpty())
                throw IllegalStateException("Duplicate schema id registration: '$id'")
        } else {
            ids[id] = schema
            schemaValidatorsById[id] = validator
        }
    }

    fun baseUri(id: URI?) = id?.let { baseUri.resolveSafe(id) } ?: baseUri
}