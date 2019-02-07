package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.builder.ValueBuilder
import kotlin.reflect.jvm.jvmName

enum class AcceptKind {
    SINGLE,
    STRUCTURE,
    NOT_ACCEPTED
}

abstract class MapperType {
    /** For all types. */
    abstract fun accepts(token: JsonTokenData): AcceptKind

    /** For all acceptKind = STRUCTURED */
    abstract fun createBuilder(token: JsonTokenData, location: JsonTokenLocation):
        ValueBuilder<out StructuredType>

    /** For all acceptKind = STRUCTURED */
    abstract fun isComplete(token: JsonTokenData): Boolean

    /** For all acceptKind = SINGLE */
    abstract fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any?

    abstract fun write(value: Any?, consumer: JsonTokenDataConsumer)

    override fun toString(): String {
        return this::class.simpleName ?: this::class.qualifiedName ?: this::class.jvmName
    }
}

abstract class StructuredType : MapperType() {
    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? =
        throw UnsupportedOperationException("in ${this::class} at $token")
}
