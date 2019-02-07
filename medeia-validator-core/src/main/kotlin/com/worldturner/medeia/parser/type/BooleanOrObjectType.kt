package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.builder.ValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import com.worldturner.medeia.parser.type.AcceptKind.SINGLE
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE
import com.worldturner.medeia.reflection.constructKotlinInstance
import com.worldturner.medeia.reflection.convertType

open class BooleanOrObjectType(
    val objectType: ObjectType,
    val kotlinBooleanProperty: String
) : StructuredType() {
    override fun accepts(token: JsonTokenData) =
        when {
            token.type == JsonTokenType.START_OBJECT -> STRUCTURE
            token.type.booleanToken -> SINGLE
            else -> NOT_ACCEPTED
        }

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation): ValueBuilder<out StructuredType> =
        objectType.createBuilder(token, location)

    override fun isComplete(token: JsonTokenData): Boolean = objectType.isComplete(token)
    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? {
        val kotlinArguments = listOfNotNull(
            kotlinBooleanProperty to token.toBoolean(),
            objectType.kotlinJsonPointerProperty?.let { it to location.pointer }
        ).toMap()
        return constructKotlinInstance(objectType.kotlinClass, kotlinArguments, token)
    }

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else {
            val kotlinProperties = prepareProperties(value)
            val booleanValue = reflectProperty(value, kotlinProperties, kotlinBooleanProperty)
            if (booleanValue == null) {
                objectType.writeObject(value, kotlinProperties, consumer)
            } else {
                consumer.consume(convertType(booleanValue, JsonTokenType.VALUE_BOOLEAN_TRUE))
            }
        }
    }
}
