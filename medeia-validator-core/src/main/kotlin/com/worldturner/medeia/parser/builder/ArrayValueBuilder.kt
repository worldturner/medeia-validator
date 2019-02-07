package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.parser.type.MapperType

class ArrayValueBuilder(startLevel: Int, type: ArrayType) : ValueBuilder<ArrayType>(startLevel, type) {
    override var currentProperty: String?
        set(_) = throw UnsupportedOperationException("on $this")
        get() = throw UnsupportedOperationException("on $this")
    val list = mutableListOf<Any?>()

    override fun add(value: Any?) {
        list.add(value)
    }

    override fun itemType(): MapperType = type.itemType()

    override fun completed(token: JsonTokenData): Boolean = type.isComplete(token)

    override fun createValue(lastToken: JsonTokenData): Any? = list
}
