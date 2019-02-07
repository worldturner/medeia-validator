package com.worldturner.medeia.schema.model

import com.worldturner.medeia.schema.validation.SchemaValidator
import java.net.URI

interface Schema {
    fun buildValidator(context: ValidationBuilderContext): SchemaValidator
    var resolvedId: URI?
}

fun <X> Map<X, PropertyNamesOrJsonSchema>.buildValidators2(context: ValidationBuilderContext) =
    mapValues { (_, alternatives) -> alternatives.buildValidator(context) }

fun <X> Map<X, Schema>.buildValidators(context: ValidationBuilderContext) =
    mapValues { (_, schema) -> schema.buildValidator(context) }

fun List<Schema>.buildValidators(context: ValidationBuilderContext) =
    map { schema -> schema.buildValidator(context) }
