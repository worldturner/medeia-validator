package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.builder.AlternativesBuilder
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import com.worldturner.medeia.reflection.constructKotlinInstance
import com.worldturner.medeia.types.Alternatives
import kotlin.reflect.KClass

class AlternativesType(
    val aType: MapperType,
    val bType: MapperType,
    val kotlinClass: KClass<out Alternatives<*, *>> = Alternatives::class
) : StructuredType() {
    override fun accepts(token: JsonTokenData): AcceptKind {
        val acceptsA = aType.accepts(token)
        if (acceptsA == NOT_ACCEPTED) {
            return bType.accepts(token)
        } else {
            return acceptsA
        }
    }

    // TODO: original choice of alternative cannot be remembered, ideally we do. In practice though,
    // TODO: this method can return true on any type.type.isLastStructureTokenOnly()
    override fun isComplete(token: JsonTokenData): Boolean = token.type.lastStructureToken

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) = run {
        val (builder, kotlinProperty) =
            if (aType.accepts(token) != NOT_ACCEPTED) {
                aType.createBuilder(token, location) to "a"
            } else {
                bType.createBuilder(token, location) to "b"
            }
        AlternativesBuilder(location.level, kotlinProperty, builder, this)
    }

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Any? {
        val acceptsA = aType.accepts(token)
        val (value, kotlinProperty) = if (acceptsA == NOT_ACCEPTED) {
            bType.createObject(token, location) to "b"
        } else {
            aType.createObject(token, location) to "a"
        }
        val kotlinArguments = mapOf(kotlinProperty to value)
        return constructKotlinInstance(kotlinClass, kotlinArguments, token)
    }

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else if (value is Alternatives<*, *>) {
            val a = value.a
            val b = value.b
            a?.let { aType.write(a, consumer) }
            b?.let { bType.write(b, consumer) }
        }
    }
}