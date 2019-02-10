package com.worldturner.medeia.schema.model

import com.worldturner.medeia.api.JsonSchemaValidationOptions
import com.worldturner.medeia.schema.EMPTY_URI
import com.worldturner.medeia.schema.resolveSafe
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.net.URI

class ValidationBuilderContext(
    val root: Boolean = true,
    val baseUri: URI = EMPTY_URI,
    val ids: MutableMap<URI, JsonSchema> = mutableMapOf(),
    val schemaValidatorsById: MutableMap<URI, SchemaValidator> = mutableMapOf(),
    val parents: List<JsonSchema> = emptyList(),
    val options: JsonSchemaValidationOptions = JsonSchemaValidationOptions.DEFAULT
) {
    fun withBaseUri(baseUri: URI, root: Boolean = false) =
        ValidationBuilderContext(
            root = root,
            baseUri = baseUri,
            ids = ids,
            schemaValidatorsById = schemaValidatorsById,
            parents = parents
        )

    fun withParent(parent: JsonSchema) =
        ValidationBuilderContext(
            root = root,
            baseUri = baseUri,
            ids = ids,
            schemaValidatorsById = schemaValidatorsById,
            parents = parents + parent
        )

    fun put(id: URI, schema: JsonSchema, validator: SchemaValidator) {
        if (id in ids) {
            throw IllegalStateException("Duplicate schema id registration: $id")
        }
        ids[id] = schema
        schemaValidatorsById[id] = validator
    }

    fun baseUri(id: URI?) = id?.let { baseUri.resolveSafe(id) } ?: baseUri
}