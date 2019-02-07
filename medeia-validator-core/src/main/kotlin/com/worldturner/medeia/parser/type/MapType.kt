package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.builder.MapValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE

class MapType(val propertyType: MapperType) : StructuredType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type == JsonTokenType.START_OBJECT) STRUCTURE else NOT_ACCEPTED

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        MapValueBuilder(location.level, this)

    override fun isComplete(token: JsonTokenData): Boolean = token.type == JsonTokenType.END_OBJECT

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else if (value is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            writeObject(value as Map<Any, Any?>, consumer)
        }
    }

    fun writeObject(
        value: Map<Any, Any?>,
        consumer: JsonTokenDataConsumer
    ) {

        consumer.consume(TOKEN_START_OBJECT)

        value.forEach { (propertyName, propertyValue) ->
            if (propertyValue == null) {
                consumer.consume(TOKEN_NULL)
            } else {
                consumer.consume(
                    JsonTokenData(
                        type = JsonTokenType.FIELD_NAME, text = propertyName.toString()
                    )
                )

                propertyType.write(propertyValue, consumer)
            }
        }

        consumer.consume(TOKEN_END_OBJECT)
    }
}