package com.worldturner.medeia.api

import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT04
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT07
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.SimpleObjectMapper
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.Schema
import com.worldturner.medeia.schema.model.SchemaWithBaseUri
import com.worldturner.medeia.schema.model.ValidationBuilderContext
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
import com.worldturner.medeia.schema.validation.SchemaValidator
import java.net.URI

private val JsonSchemaVersion.mapperType: MapperType
    get() =
        when (this) {
            DRAFT04 -> JsonSchemaDraft04Type
            DRAFT07 -> JsonSchemaDraft07Type
        }

abstract class MedeiaApiBase {

    fun loadSchemas(sources: SchemaSources, options: JsonSchemaValidationOptions) =
        loadSchemas(sources, validatorMap = null, options = options)

    @JvmOverloads
    fun loadSchemas(
        sources: SchemaSources,
        validatorMap: MutableMap<URI, SchemaValidator>? = null,
        options: JsonSchemaValidationOptions = JsonSchemaValidationOptions.DEFAULT
    ): SchemaValidator {
        if (sources.isEmpty())
            throw IllegalArgumentException("Need at least one schema source")
        val parsedSchemas = sources.sources.map { loadSchema(it) }
        val context = ValidationBuilderContext(options = options)
        val validators = parsedSchemas.map { it.buildValidator(context) }
        validatorMap?.let {
            it.putAll(context.schemaValidatorsById)
        }
        return validators.first()
    }

    protected abstract fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter

    private fun loadSchema(source: SchemaSource): Schema {
        val consumer = SimpleObjectMapper(source.version.mapperType, 0)
        val parser: JsonParserAdapter = createSchemaParser(source, consumer)
        parser.parseAll()
        val schema = consumer.takeResult() as JsonSchema
        val augmentedSchema = source.baseUri?.let { SchemaWithBaseUri(it, schema) } ?: schema
        return augmentedSchema
    }
}