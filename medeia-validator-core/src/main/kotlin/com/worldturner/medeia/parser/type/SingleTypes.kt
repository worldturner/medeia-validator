package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.builder.ValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import com.worldturner.medeia.parser.type.AcceptKind.SINGLE
import com.worldturner.medeia.reflection.convertType

abstract class SingleType : MapperType() {
    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation):
        ValueBuilder<out StructuredType> = throw UnsupportedOperationException()

    override fun isComplete(token: JsonTokenData): Boolean = true
}

object TextType : SingleType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type == JsonTokenType.VALUE_TEXT) SINGLE else NOT_ACCEPTED

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): String = token.text!!

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        consumer.consume(convertType(value, JsonTokenType.VALUE_TEXT))
    }
}

object NumberType : SingleType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type == JsonTokenType.VALUE_NUMBER) SINGLE else NOT_ACCEPTED

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Number = token.toDecimal()

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        consumer.consume(convertType(value, JsonTokenType.VALUE_NUMBER))
    }
}

object BooleanType : SingleType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type.booleanToken) SINGLE else NOT_ACCEPTED

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Boolean =
        token.toBoolean()

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        consumer.consume(convertType(value, JsonTokenType.VALUE_BOOLEAN_TRUE))
    }
}

object NullType : SingleType() {
    override fun accepts(token: JsonTokenData) =
        if (token.type == JsonTokenType.VALUE_NULL) SINGLE else NOT_ACCEPTED

    override fun createObject(token: JsonTokenData, location: JsonTokenLocation): Unit? = null

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        consumer.consume(convertType(value, JsonTokenType.VALUE_NULL))
    }
}
