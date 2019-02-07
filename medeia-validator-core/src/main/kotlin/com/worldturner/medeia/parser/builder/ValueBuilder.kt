package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.type.MapperType

abstract class ValueBuilder<T : MapperType>(val startLevel: Int, protected val type: T) {
    abstract fun add(value: Any?)
    abstract var currentProperty: String?
    abstract fun itemType(): MapperType
    abstract fun completed(token: JsonTokenData): Boolean
    abstract fun createValue(lastToken: JsonTokenData): Any?
    open val consumerBuilder: JsonTokenDataAndLocationBuilder?
        get() = null

    override fun toString(): String {
        return "${this::class.simpleName}(startLevel=$startLevel, type=$type)"
    }
}