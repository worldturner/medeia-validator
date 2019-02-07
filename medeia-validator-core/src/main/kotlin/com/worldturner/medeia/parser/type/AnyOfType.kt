package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import kotlin.reflect.KClass

/**
 * Only supports writing output, not parsing.
 */
open class AnyOfType(val classMap: Map<KClass<*>, MapperType>) : StructuredType() {
    override fun accepts(token: JsonTokenData): AcceptKind = NOT_ACCEPTED
    override fun isComplete(token: JsonTokenData): Boolean = false
    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        throw UnsupportedOperationException()

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation) =
        throw UnsupportedOperationException()

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else {
            val mapperType = classMap[value] ?: classMap.filterKeys { kotlinType ->
                kotlinType.isInstance(value)
            }.values.first()
            mapperType.write(value, consumer)
        }
    }
}