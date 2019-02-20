package com.worldturner.medeia.schema.parser

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
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.PropertyNamesOrJsonSchema

object JsonSchemaDraft07TypeCore : ObjectType(
    kotlinClass = JsonSchema::class,
    propertyTypes = listOf(
        PropertyType("\$schema", TextType, "schema"),
        PropertyType("\$id", TextType, "id"),
        PropertyType("\$ref", TextType, "ref"),
        PropertyType("\$comment", TextType, "comment"),
        PropertyType("title", TextType),
        PropertyType("description", TextType),
        PropertyType("default", SimpleTreeType),
        PropertyType("readOnly", BooleanType),
        PropertyType("examples", ArrayType(SimpleTreeType)),
        PropertyType("multipleOf", NumberType),
        PropertyType("maximum", NumberType),
        PropertyType("exclusiveMaximum", NumberType),
        PropertyType("minimum", NumberType),
        PropertyType("exclusiveMinimum", NumberType),
        PropertyType("maxLength", NumberType),
        PropertyType("minLength", NumberType),
        PropertyType("pattern", TextType),
        PropertyType("additionalItems", JsonSchemaDraft07TypeReference),
        PropertyType("items", SingleOrArrayType(JsonSchemaDraft07TypeReference)),
        PropertyType("maxItems", NumberType),
        PropertyType("minItems", NumberType),
        PropertyType("uniqueItems", BooleanType),
        PropertyType("contains", JsonSchemaDraft07TypeReference),
        PropertyType("maxProperties", NumberType),
        PropertyType("minProperties", NumberType),
        PropertyType("required", ArrayType(TextType)),
        PropertyType("additionalProperties", JsonSchemaDraft07TypeReference),
        PropertyType("properties", MapType(JsonSchemaDraft07TypeReference)),
        PropertyType("patternProperties", MapType(JsonSchemaDraft07TypeReference)),
        PropertyType(
            "dependencies",
            MapType(
                AlternativesType(
                    ArrayType(TextType),
                    JsonSchemaDraft07TypeReference,
                    kotlinClass = PropertyNamesOrJsonSchema::class
                )
            )
        ),
        PropertyType("propertyNames", JsonSchemaDraft07TypeReference),
        PropertyType("const", SimpleTreeType),
        PropertyType("enum", ArrayType(SimpleTreeType)),
        PropertyType("type", ArrayType(TextType, allowSingleValue = true)),
        PropertyType("format", TextType),
        PropertyType("contentMediaType", TextType),
        PropertyType("contentEncoding", TextType),
        PropertyType("definitions", MapType(JsonSchemaDraft07TypeReference)),
        PropertyType("if", JsonSchemaDraft07TypeReference, "ifSchema"),
        PropertyType("then", JsonSchemaDraft07TypeReference, "thenSchema"),
        PropertyType("else", JsonSchemaDraft07TypeReference, "elseSchema"),
        PropertyType("allOf", ArrayType(JsonSchemaDraft07TypeReference)),
        PropertyType("anyOf", ArrayType(JsonSchemaDraft07TypeReference)),
        PropertyType("oneOf", ArrayType(JsonSchemaDraft07TypeReference)),
        PropertyType("not", JsonSchemaDraft07TypeReference)
    ),
    ignoreAdditionalProperties = true,
    kotlinJsonPointerProperty = "jsonPointer"
)

object JsonSchemaDraft07Type : BooleanOrObjectType(JsonSchemaDraft07TypeCore, "acceptAllOrNothing")

object JsonSchemaDraft07TypeReference : ReferenceType({ JsonSchemaDraft07Type })
