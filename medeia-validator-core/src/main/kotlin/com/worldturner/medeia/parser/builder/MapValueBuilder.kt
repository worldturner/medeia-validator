package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.type.MapType
import com.worldturner.medeia.parser.type.MapperType

class MapValueBuilder(startLevel: Int, type: MapType) : ValueBuilder<MapType>(startLevel, type) {
    override var currentProperty: String? = null
    var map = mutableMapOf<String, Any?>()

    override fun add(value: Any?) {
        map[currentProperty!!] = value
        currentProperty = null
    }

    override fun itemType(): MapperType = type.propertyType

    override fun completed(token: JsonTokenData): Boolean = type.isComplete(token)

    override fun createValue(lastToken: JsonTokenData): Any? = map
}
