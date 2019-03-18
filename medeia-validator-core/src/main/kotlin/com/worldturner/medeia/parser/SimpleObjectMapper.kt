package com.worldturner.medeia.parser

import com.worldturner.medeia.parser.builder.ValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.util.getAndClear
import java.util.ArrayDeque

class SimpleObjectMapper(val rootType: MapperType, val startLevel: Int) : JsonTokenDataAndLocationBuilder {
    private var result: Any? = null

    private val valueBuilderStack =
        ArrayDeque<ValueBuilder<out MapperType>>().apply { push(RootBuilder()) }

    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        try {
            consumeProtected(token, location)
        } catch (e: TokenLocationException) {
            throw e
        } catch (e: Exception) {
            throw TokenLocationException(e.message, location.toString(), e)
        }
    }

    private fun consumeProtected(
        token: JsonTokenData,
        location: JsonTokenLocation
    ) {
        val top = valueBuilderStack.peek()
        val consumerBuilder = top.consumerBuilder
        if (consumerBuilder == null && token.type == JsonTokenType.FIELD_NAME) {
            top.currentProperty = token.text!!
        } else if (top.startLevel == location.level && top.completed(token)) {
            valueBuilderStack.pop()
            if (consumerBuilder != null) {
                // Consume final type if necessary
                consumerBuilder.consume(token, location)
            }
            val value = top.createValue(token)
            completeValue(value)
        } else if (consumerBuilder != null) {
            // Consumer builders eat all tokens
            consumerBuilder.consume(token, location)
        } else {
            val currentType = top.itemType()
            val acceptKind = currentType.accepts(token)
            when (acceptKind) {
                AcceptKind.SINGLE -> completeValue(currentType.createObject(token, location))
                AcceptKind.STRUCTURE -> {
                    val createdBuilder = currentType.createBuilder(token, location)
                    valueBuilderStack.push(createdBuilder)
                    // If it's a consumerBuilder, pass current type to it
                    val createdConsumerBuilder = createdBuilder.consumerBuilder
                    if (createdConsumerBuilder != null) {
                        createdConsumerBuilder.consume(token, location)
                    }
                }
                else ->
                    throw TypeMismatchException("$token not accepted by $currentType", location.toString())
            }
        }
    }

    private fun completeValue(value: Any?) {
        if (valueBuilderStack.isEmpty()) {
            result = value
        } else {
            val builder = valueBuilderStack.peek()!!
            builder.add(value)
        }
    }

    override fun takeResult(): Any? = ::result.getAndClear()

    // A stand-in for the result of the entire builder
    private inner class RootBuilder : ValueBuilder<MapperType>(startLevel, rootType) {
        override var currentProperty: String?
            set(_) = throw UnsupportedOperationException("on $this")
            get() = throw UnsupportedOperationException("on $this")

        override fun add(value: Any?) {
            this@SimpleObjectMapper.result = value
        }

        override fun createValue(lastToken: JsonTokenData): Any? = this@SimpleObjectMapper.result

        override fun itemType(): MapperType = rootType
        override fun completed(token: JsonTokenData): Boolean = false
    }
}

open class TokenLocationException(
    message: String?,
    val location: String,
    cause: Throwable? = null
) :
    RuntimeException("$message $location", cause)

class TypeMismatchException(message: String, location: String) : TokenLocationException(message, location)