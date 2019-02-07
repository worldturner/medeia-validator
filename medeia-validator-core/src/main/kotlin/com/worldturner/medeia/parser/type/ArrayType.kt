package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.builder.ArrayValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE

class ArrayType(
    val itemType: MapperType,
    val allowSingleValue: Boolean = false
) : StructuredType() {

    fun itemType(): MapperType = itemType

    override fun accepts(token: JsonTokenData) =
        if (token.type == JsonTokenType.START_ARRAY)
            STRUCTURE
        else if (allowSingleValue)
            itemType.accepts(token)
        else
            NOT_ACCEPTED

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        ArrayValueBuilder(location.level, this)

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? =
        listOf(itemType.createObject(token, location))

    override fun isComplete(token: JsonTokenData): Boolean = token.type == JsonTokenType.END_ARRAY

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else if (value is Collection<*>) {
            if (allowSingleValue && value.size == 1) {
                itemType.write(value.first(), consumer)
            } else {
                consumer.consume(TOKEN_START_ARRAY)
                value.forEach {
                    itemType.write(it, consumer)
                }
                consumer.consume(TOKEN_END_ARRAY)
            }
        }
    }
}