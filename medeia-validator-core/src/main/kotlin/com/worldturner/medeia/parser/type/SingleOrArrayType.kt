package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.builder.ArrayValueBuilder
import com.worldturner.medeia.parser.builder.SingleOrArrayValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE
import com.worldturner.medeia.types.SingleOrList

class SingleOrArrayType(val type: MapperType) : StructuredType() {
    val arrayType = ArrayType(type)
    override fun accepts(token: JsonTokenData) =
        if (token.type == JsonTokenType.START_ARRAY) STRUCTURE else type.accepts(token)

    override fun isComplete(token: JsonTokenData): Boolean =
        token.type == JsonTokenType.END_ARRAY || type.isComplete(token)

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        when (token.type) {
            JsonTokenType.START_ARRAY ->
                SingleOrArrayValueBuilder(
                    single = false,
                    builder = ArrayValueBuilder(location.level, arrayType),
                    type = this,
                    startLevel = location.level
                )
            else ->
                SingleOrArrayValueBuilder(
                    single = true,
                    builder = type.createBuilder(token, location),
                    type = this,
                    startLevel = location.level
                )
        }

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? =
        SingleOrList(single = type.createObject(token, location))

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value is SingleOrList<*>) {
            value.list?.let { arrayType.write(it, consumer) }
            value.single?.let { type.write(it, consumer) }
        }
    }
}