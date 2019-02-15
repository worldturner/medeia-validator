package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.END_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.END_OBJECT
import com.worldturner.medeia.parser.JsonTokenType.END_OF_STREAM
import com.worldturner.medeia.parser.JsonTokenType.FIELD_NAME
import com.worldturner.medeia.parser.JsonTokenType.NONE
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_OBJECT
import com.worldturner.medeia.parser.JsonTokenType.VALUE_BOOLEAN_FALSE
import com.worldturner.medeia.parser.JsonTokenType.VALUE_BOOLEAN_TRUE
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NULL
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NUMBER
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.schema.model.SimpleType
import com.worldturner.medeia.schema.model.SimpleType.ARRAY
import com.worldturner.medeia.schema.model.SimpleType.BOOLEAN
import com.worldturner.medeia.schema.model.SimpleType.INTEGER
import com.worldturner.medeia.schema.model.SimpleType.NULL
import com.worldturner.medeia.schema.model.SimpleType.NUMBER
import com.worldturner.medeia.schema.model.SimpleType.OBJECT
import com.worldturner.medeia.schema.model.SimpleType.STRING
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.net.URI
import java.util.EnumSet

class TypeValidator(
    val type: EnumSet<SimpleType>
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun recordUnknownRefs(unknownRefs: MutableCollection<URI>) = Unit

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        return type.let {
            if (when (token.type) {
                    START_ARRAY -> it.contains(SimpleType.ARRAY)
                    VALUE_TEXT -> it.contains(SimpleType.STRING)
                    VALUE_NULL -> it.contains(SimpleType.NULL)
                    VALUE_NUMBER ->
                        it.contains(SimpleType.NUMBER) ||
                            (it.contains(SimpleType.INTEGER) && token.isInteger())
                    VALUE_BOOLEAN_TRUE, VALUE_BOOLEAN_FALSE -> it.contains(SimpleType.BOOLEAN)
                    START_OBJECT -> it.contains(SimpleType.OBJECT)
                    else -> false
                }
            ) {
                OkValidationResult
            } else {
                FailedValidationResult(
                    location = location,
                    rule = "type",
                    message = "Type mismatch, data has ${token.schemaType} and schema has " +
                        "${if (type.size == 1) type.first() else type}"
                )
            }
        }
    }

    companion object {
        fun create(type: EnumSet<SimpleType>?) =
            type?.let { TypeValidator(type) }
    }
}

val JsonTokenData.schemaType: SimpleType?
    get() = when (this.type) {
        START_ARRAY, END_ARRAY -> ARRAY
        START_OBJECT, END_OBJECT -> OBJECT
        VALUE_BOOLEAN_FALSE, VALUE_BOOLEAN_TRUE -> BOOLEAN
        VALUE_NULL -> NULL
        VALUE_NUMBER -> if (isInteger()) INTEGER else NUMBER
        VALUE_TEXT -> STRING
        FIELD_NAME, END_OF_STREAM, NONE -> null
    }