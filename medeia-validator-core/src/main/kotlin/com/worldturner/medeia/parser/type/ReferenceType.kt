package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.builder.ValueBuilder

open class ReferenceType(val typeReference: () -> MapperType) : MapperType() {
    private var delegateField: MapperType? = null

    val delegate: MapperType
        get() {
            if (delegateField == null)
                delegateField = typeReference()
            return delegateField!!
        }

    override fun accepts(token: JsonTokenData): AcceptKind = delegate.accepts(token)
    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation): ValueBuilder<out StructuredType> =
        delegate.createBuilder(token, location)

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? =
        delegate.createObject(token, location)

    override fun isComplete(token: JsonTokenData): Boolean = delegate.isComplete(token)
    override fun write(value: Any?, consumer: JsonTokenDataConsumer) =
        delegate.write(value, consumer)
}