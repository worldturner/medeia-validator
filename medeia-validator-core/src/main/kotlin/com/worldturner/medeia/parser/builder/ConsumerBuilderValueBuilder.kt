package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.parser.type.SimpleTreeType

class ConsumerBuilderValueBuilder(
    startLevel: Int,
    override val consumerBuilder: JsonTokenDataAndLocationBuilder
) : ValueBuilder<SimpleTreeType>(startLevel, SimpleTreeType) {

    override var currentProperty: String?
        set(_) = throw UnsupportedOperationException("on $this")
        get() = throw UnsupportedOperationException("on $this")

    override fun add(value: Any?) = throw UnsupportedOperationException()

    override fun itemType(): MapperType = throw UnsupportedOperationException()

    override fun completed(token: JsonTokenData): Boolean = true

    override fun createValue(lastToken: JsonTokenData): Any? = consumerBuilder.takeResult()
}