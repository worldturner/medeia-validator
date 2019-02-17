package com.worldturner.medeia.schema.parser

import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.type.AlternativesType
import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.parser.type.BooleanOrObjectType
import com.worldturner.medeia.parser.type.BooleanType
import com.worldturner.medeia.parser.type.MapType
import com.worldturner.medeia.parser.type.NumberType
import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.parser.type.PropertyType
import com.worldturner.medeia.parser.type.ReferenceType
import com.worldturner.medeia.parser.type.SimpleTreeType
import com.worldturner.medeia.parser.type.SingleOrArrayType
import com.worldturner.medeia.parser.type.TextType
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.PropertyNamesOrJsonSchema
import com.worldturner.medeia.schema.model.SimpleType
import com.worldturner.medeia.types.SingleOrList
import java.math.BigDecimal
import java.net.URI
import java.util.EnumSet

object JsonSchemaDraft04TypeCore : ObjectType(
    kotlinClass = JsonSchema::class,
    propertyTypes = listOf(
        PropertyType("\$schema", TextType, "schema"),
        PropertyType("id", TextType),
        PropertyType("\$ref", TextType, "ref"),
        PropertyType("\$comment", TextType, "comment"),
        PropertyType("title", TextType),
        PropertyType("description", TextType),
        PropertyType("default", SimpleTreeType),
        PropertyType("readOnly", BooleanType),
        PropertyType("multipleOf", NumberType),
        PropertyType("maximum", NumberType),
        PropertyType("exclusiveMaximum", BooleanType),
        PropertyType("minimum", NumberType),
        PropertyType("exclusiveMinimum", BooleanType),
        PropertyType("maxLength", NumberType),
        PropertyType("minLength", NumberType),
        PropertyType("pattern", TextType),
        PropertyType("additionalItems", JsonSchemaDraft04TypeReference),
        PropertyType("items", SingleOrArrayType(JsonSchemaDraft04TypeReference)),
        PropertyType("maxItems", NumberType),
        PropertyType("minItems", NumberType),
        PropertyType("uniqueItems", BooleanType),
        PropertyType("maxProperties", NumberType),
        PropertyType("minProperties", NumberType),
        PropertyType("required", ArrayType(TextType)),
        PropertyType("additionalProperties", JsonSchemaDraft04TypeReference),
        PropertyType("properties", MapType(JsonSchemaDraft04TypeReference)),
        PropertyType("patternProperties", MapType(JsonSchemaDraft04TypeReference)),
        PropertyType(
            "dependencies",
            MapType(
                AlternativesType(
                    ArrayType(TextType),
                    JsonSchemaDraft04TypeReference,
                    kotlinClass = PropertyNamesOrJsonSchema::class
                )
            )
        ),
        PropertyType("enum", ArrayType(SimpleTreeType)),
        PropertyType("type", ArrayType(TextType, allowSingleValue = true)),
        PropertyType("format", TextType),
        PropertyType("contentMediaType", TextType),
        PropertyType("contentEncoding", TextType),
        PropertyType("definitions", MapType(JsonSchemaDraft04TypeReference)),
        PropertyType("if", JsonSchemaDraft04TypeReference, "if_"),
        PropertyType("then", JsonSchemaDraft04TypeReference, "then_"),
        PropertyType("else", JsonSchemaDraft04TypeReference, "else_"),
        PropertyType("allOf", ArrayType(JsonSchemaDraft04TypeReference)),
        PropertyType("anyOf", ArrayType(JsonSchemaDraft04TypeReference)),
        PropertyType("oneOf", ArrayType(JsonSchemaDraft04TypeReference)),
        PropertyType("not", JsonSchemaDraft04TypeReference)
    ),
    ignoreAdditionalProperties = true,
    kotlinJsonPointerProperty = "jsonPointer",
    kotlinConstructors = listOf(::createJsonSchemaDraft04)
)

object JsonSchemaDraft04Type : BooleanOrObjectType(JsonSchemaDraft04TypeCore, "acceptAllOrNothing")

object JsonSchemaDraft04TypeReference : ReferenceType({ JsonSchemaDraft04Type })

fun createJsonSchemaDraft04(
    schema: URI? = null,
    id: URI? = null,
    ref: URI? = null,
    comment: String? = null,
    title: String? = null,
    description: String? = null,
    default: NodeData? = null,
    readOnly: Boolean? = null,
    multipleOf: BigDecimal? = null,
    maximum: BigDecimal? = null,
    exclusiveMaximum: Boolean? = null,
    minimum: BigDecimal? = null,
    exclusiveMinimum: Boolean? = null,
    maxLength: Int? = null,
    minLength: Int? = null,
    pattern: Regex? = null,
    additionalItems: JsonSchema? = null,
    items: SingleOrList<JsonSchema>? = null,
    maxItems: Int? = null,
    minItems: Int? = null,
    uniqueItems: Boolean? = null,
    maxProperties: Int? = null,
    minProperties: Int? = null,
    required: Set<String>? = null,
    additionalProperties: JsonSchema? = null,
    properties: Map<String, JsonSchema>? = null,
    patternProperties: Map<Regex, JsonSchema>? = null,
    dependencies: Map<String, PropertyNamesOrJsonSchema>? = null,
    propertyNames: JsonSchema? = null,
    enum: Set<NodeData>? = null,
    type: EnumSet<SimpleType>? = null,
    format: String? = null,
    contentMediaType: String? = null,
    contentEncoding: String? = null,
    definitions: Map<String, JsonSchema>? = null,
    if_: JsonSchema? = null,
    then_: JsonSchema? = null,
    else_: JsonSchema? = null,
    allOf: List<JsonSchema>? = null,
    anyOf: List<JsonSchema>? = null,
    oneOf: List<JsonSchema>? = null,
    not: JsonSchema? = null,
    acceptAllOrNothing: Boolean? = null,
    jsonPointer: JsonPointer
): JsonSchema {
    return JsonSchema(
        schema = schema,
        id = id,
        ref = ref,
        comment = comment,
        title = title,
        description = description,
        default = default,
        readOnly = readOnly,
        multipleOf = multipleOf,
        maximum = if (exclusiveMaximum == true) null else maximum,
        exclusiveMaximum = if (exclusiveMaximum == true) maximum else null,
        minimum = if (exclusiveMinimum == true) null else minimum,
        exclusiveMinimum = if (exclusiveMinimum == true) minimum else null,
        maxLength = maxLength,
        minLength = minLength,
        pattern = pattern,
        additionalItems = additionalItems,
        items = items,
        maxItems = maxItems,
        minItems = minItems,
        uniqueItems = uniqueItems,
        maxProperties = maxProperties,
        minProperties = minProperties,
        required = required,
        additionalProperties = additionalProperties,
        properties = properties,
        patternProperties = patternProperties,
        dependencies = dependencies,
        propertyNames = propertyNames,
        enum = enum,
        type = type,
        format = format,
        contentMediaType = contentMediaType,
        contentEncoding = contentEncoding,
        definitions = definitions,
        if_ = if_,
        then_ = then_,
        else_ = else_,
        allOf = allOf,
        anyOf = anyOf,
        oneOf = oneOf,
        not = not,
        acceptAllOrNothing = acceptAllOrNothing,
        jsonPointer = jsonPointer
    )
}
