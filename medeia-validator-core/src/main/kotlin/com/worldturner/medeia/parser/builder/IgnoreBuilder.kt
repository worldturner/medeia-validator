package com.worldturner.medeia.parser.builder

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.JsonTokenLocation

class IgnoreBuilder : JsonTokenDataAndLocationBuilder {
    override fun consume(token: JsonTokenData, location: JsonTokenLocation) = Unit
    override fun takeResult(): Any? = null
}