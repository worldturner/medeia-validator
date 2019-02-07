package com.worldturner.medeia.parser

interface JsonTokenDataConsumer : JsonTokenDataAndLocationConsumer {
    fun consume(token: JsonTokenData)

    override fun consume(token: JsonTokenData, location: JsonTokenLocation) = consume(token)
}

interface JsonTokenDataAndLocationConsumer {
    fun consume(token: JsonTokenData, location: JsonTokenLocation)
}

interface JsonTokenDataBuilder : JsonTokenDataAndLocationBuilder, JsonTokenDataConsumer {
    override fun takeResult(): Any?
}

interface JsonTokenDataAndLocationBuilder : JsonTokenDataAndLocationConsumer {
    fun takeResult(): Any?
}
