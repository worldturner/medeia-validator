package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.pointer.JsonPointer

class ObjectValueBuilder(startLevel: Int, val pointer: JsonPointer?, type: ObjectType) :
    ValueBuilder<ObjectType>(startLevel, type) {
    override var currentProperty: String? = null
    var map = mutableMapOf<String, Any?>()
    override fun add(value: Any?) {
        map[currentProperty!!] = value
        currentProperty = null
    }

    override fun itemType(): MapperType = type.itemType(currentProperty!!)

    override fun completed(token: JsonTokenData): Boolean = type.isComplete(token)

    override fun createValue(lastToken: JsonTokenData): Any? = type.createValue(lastToken, pointer, map)
}