package com.worldturner.medeia.schema.model

import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.pointer.hasJsonPointerFragment
import com.worldturner.medeia.schema.validation.ArrayUniqueItemsValidator
import com.worldturner.medeia.schema.validation.ArrayValidator
import com.worldturner.medeia.schema.validation.BooleanValueValidator
import com.worldturner.medeia.schema.validation.ConstValidator
import com.worldturner.medeia.schema.validation.ContentValidator
import com.worldturner.medeia.schema.validation.EnumValidator
import com.worldturner.medeia.schema.validation.ExistentialOperation.ALL_OF
import com.worldturner.medeia.schema.validation.ExistentialOperation.ANY_OF
import com.worldturner.medeia.schema.validation.ExistentialOperation.ONE_OF
import com.worldturner.medeia.schema.validation.ExistentialValidator
import com.worldturner.medeia.schema.validation.FormatValidator
import com.worldturner.medeia.schema.validation.IfThenElseValidator
import com.worldturner.medeia.schema.validation.NotValidator
import com.worldturner.medeia.schema.validation.NumberValidator
import com.worldturner.medeia.schema.validation.ObjectValidator
import com.worldturner.medeia.schema.validation.RefSchemaValidator
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.StringValidator
import com.worldturner.medeia.schema.validation.TypeValidator
import com.worldturner.medeia.types.Alternatives
import com.worldturner.medeia.types.SingleOrList
import com.worldturner.util.hasAnyOtherThanFragment
import com.worldturner.util.hasFragment
import com.worldturner.util.orNull
import com.worldturner.util.replaceFragment
import com.worldturner.util.resolveSafe
import com.worldturner.util.withEmptyFragment
import java.math.BigDecimal
import java.net.URI
import java.util.EnumSet
import java.util.Locale

data class JsonSchema constructor(
    val schema: URI? = null,
    val id: URI? = null,
    val ref: URI? = null,
    val comment: String? = null,
    val title: String? = null,
    val description: String? = null,
    val default: NodeData? = null,
    val readOnly: Boolean? = null,
    val examples: List<NodeData>? = null,
    val multipleOf: BigDecimal? = null,
    val maximum: BigDecimal? = null,
    val exclusiveMaximum: BigDecimal? = null,
    val minimum: BigDecimal? = null,
    val exclusiveMinimum: BigDecimal? = null,
    val maxLength: Int? = null,
    val minLength: Int? = null,
    val pattern: Regex? = null,
    val additionalItems: JsonSchema? = null,
    val items: SingleOrList<JsonSchema>? = null,
    val maxItems: Int? = null,
    val minItems: Int? = null,
    val uniqueItems: Boolean? = null,
    val contains: JsonSchema? = null,
    val maxProperties: Int? = null,
    val minProperties: Int? = null,
    val required: Set<String>? = null,
    val additionalProperties: JsonSchema? = null,
    val properties: Map<String, JsonSchema>? = null,
    val patternProperties: Map<Regex, JsonSchema>? = null,
    val dependencies: Map<String, PropertyNamesOrJsonSchema>? = null,
    val propertyNames: JsonSchema? = null,
    val const: NodeData? = null,
    val enum: Set<NodeData>? = null,
    val type: EnumSet<SimpleType>? = null,
    val format: String? = null,
    val contentMediaType: String? = null,
    val contentEncoding: String? = null,
    val definitions: Map<String, JsonSchema>? = null,
    val ifSchema: JsonSchema? = null,
    val thenSchema: JsonSchema? = null,
    val elseSchema: JsonSchema? = null,
    val allOf: List<JsonSchema>? = null,
    val anyOf: List<JsonSchema>? = null,
    val oneOf: List<JsonSchema>? = null,
    val not: JsonSchema? = null,
    // When a schema is parsed as a boolean (meaning nothing or "everything")
    val acceptAllOrNothing: Boolean? = null,
    val jsonPointer: JsonPointer
) : Schema {

    override var resolvedId: URI? = null

    internal fun recordIds(
        validator: SchemaValidator,
        context: ValidationBuilderContext
    ) {
        resolvedId?.also {
            if (id != null || context.root) {
                context.put(it, this, validator)
                if (!it.hasFragment()) context.put(it.withEmptyFragment(), this, validator)
            }
        }
        context.parents.forEach { parent ->
            parent.resolvedId?.let {
                val relativeJsonPointer = parent.jsonPointer.relativize(this.jsonPointer)
                val uri = it.replaceFragment(relativeJsonPointer.toString(), encoded = true)
                context.put(uri, this, validator)
            }
        }

        definitions?.forEach { (_, schema) ->
            val subContext =
                resolvedId?.let { context.withBaseUri(it).withParent(this) } ?: context
            schema.buildValidator(subContext)
        }
    }

    override fun buildValidator(context: ValidationBuilderContext): SchemaValidator {
        if (id != null && id.hasJsonPointerFragment()) {
            if (id.hasAnyOtherThanFragment() || id.fragment != jsonPointer.toString()) {
                // Note if the id is simply trying to define the current json pointer
                // (which is invalid but strangely, commonly done) then just ignore it.
                throw IllegalArgumentException(
                    "Invalid \$id with non-plain name fragment (see section 8.2.3 of json-schema-core): '$id'"
                )
            }
        } else {
            resolvedId = id?.let {
                context.baseUri.resolveSafe(id)
            } ?: if (context.root) context.baseUri else null
        }

        ref?.let {
            val validator = RefSchemaValidator(context.baseUri.resolveSafe(ref), context.schemaValidatorsById)
            recordIds(validator, context)
            return validator
        }
        val subContext = context.withBaseUri(context.baseUri(id)).withParent(this)

        val validatorList =
            listOfNotNull(
                BooleanValueValidator.create(acceptAllOrNothing),
                TypeValidator.create(type),
                NumberValidator.create(multipleOf, maximum, exclusiveMaximum, minimum, exclusiveMinimum),
                StringValidator.create(maxLength, minLength, pattern),
                context.options.validateFormat.orNull { FormatValidator.create(format) },
                context.options.validateContent.orNull { ContentValidator.create(contentMediaType, contentEncoding) },
                ArrayValidator.create(
                    additionalItems?.let { it.buildValidator(subContext) },
                    items?.list?.let { it.buildValidators(subContext) },
                    items?.single?.let { it.buildValidator(subContext) },
                    maxItems,
                    minItems,
                    contains?.let { it.buildValidator(subContext) }),
                ArrayUniqueItemsValidator.create(
                    uniqueItems = uniqueItems,
                    method = context.options.uniqueItemsValidationMethod
                ),
                ConstValidator.create(const),
                EnumValidator.create(enum),
                ObjectValidator.create(
                    maxProperties,
                    minProperties,
                    required,
                    additionalProperties?.let { it.buildValidator(subContext) },
                    properties?.let { it.buildValidators(subContext) },
                    patternProperties?.let { it.buildValidators(subContext) },
                    propertyNames?.let { it.buildValidator(subContext) },
                    dependencies?.let { it.buildValidators2(subContext) }),
                ExistentialValidator.create(
                    ANY_OF,
                    anyOf?.map { it.buildValidator(subContext) },
                    context.options.optimizeExistentialValidators
                ),
                ExistentialValidator.create(
                    ALL_OF,
                    allOf?.map { it.buildValidator(subContext) },
                    context.options.optimizeExistentialValidators
                ),
                ExistentialValidator.create(
                    ONE_OF,
                    oneOf?.map { it.buildValidator(subContext) },
                    context.options.optimizeExistentialValidators
                ),
                NotValidator.create(not?.let { it.buildValidator(subContext) }),
                IfThenElseValidator.create(
                    ifSchema?.buildValidator(subContext),
                    thenSchema?.let { it.buildValidator(subContext) },
                    elseSchema?.let { it.buildValidator(subContext) })

            )
        val validator =
            when (validatorList.size) {
                0 -> BooleanValueValidator(true)
                1 -> validatorList.first()
                else -> ExistentialValidator.create(ALL_OF, validatorList, true)!!
            }
        recordIds(validator, context)
        return validator
    }
}

enum class SimpleType {
    ARRAY,
    BOOLEAN,
    INTEGER,
    NULL,
    NUMBER,
    OBJECT,
    STRING;

    override fun toString(): String {
        return super.toString().toLowerCase(Locale.US)
    }
}

class PropertyNamesOrJsonSchema(a: Set<String>? = null, b: JsonSchema? = null) :
    Alternatives<Set<String>, JsonSchema>(a, b)

fun PropertyNamesOrJsonSchema.buildValidator(context: ValidationBuilderContext): SchemaValidator {
    a?.let { return ObjectValidator(required = a) }
    b?.let { return b.buildValidator(context) }
    throw IllegalStateException()
}
