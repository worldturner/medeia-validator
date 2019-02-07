package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.type.AlternativesType
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.parser.type.StructuredType
import com.worldturner.medeia.reflection.constructKotlinInstance

class AlternativesBuilder(
    startLevel: Int,
    val kotlinProperty: String,
    val builder: ValueBuilder<out StructuredType>,
    type: AlternativesType
) : ValueBuilder<AlternativesType>(startLevel, type) {
    override var currentProperty: String?
        set(value) {
            builder.currentProperty = value
        }
        get() = builder.currentProperty

    override fun add(value: Any?) {
        builder.add(value)
    }

    override fun itemType(): MapperType = builder.itemType()

    override fun completed(token: JsonTokenData): Boolean = builder.completed(token)

    override fun createValue(lastToken: JsonTokenData): Any? {
        val kotlinArguments =
            mapOf(kotlinProperty to builder.createValue(lastToken))
        return constructKotlinInstance(type.kotlinClass, kotlinArguments, lastToken)
    }
}
