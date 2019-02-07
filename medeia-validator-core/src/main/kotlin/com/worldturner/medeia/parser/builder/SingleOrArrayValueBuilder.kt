package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.parser.type.SingleOrArrayType
import com.worldturner.medeia.parser.type.StructuredType
import com.worldturner.medeia.types.SingleOrList

class SingleOrArrayValueBuilder(
    startLevel: Int,
    type: SingleOrArrayType,
    val single: Boolean,
    val builder: ValueBuilder<out StructuredType>
) : ValueBuilder<StructuredType>(startLevel, type) {
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

    override fun createValue(lastToken: JsonTokenData): Any? =
        if (single) {
            SingleOrList(single = builder.createValue(lastToken))
        } else {
            SingleOrList(list = builder.createValue(lastToken) as List<*>)
        }
}
