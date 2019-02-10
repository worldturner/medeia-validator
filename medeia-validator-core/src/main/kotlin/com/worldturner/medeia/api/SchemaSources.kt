package com.worldturner.medeia.api

import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT07
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
import java.io.InputStream
import java.io.Reader
import java.net.URI

enum class JsonSchemaVersion(internal val mapperType: MapperType) {
    DRAFT04(JsonSchemaDraft04Type), DRAFT07(JsonSchemaDraft07Type)
}

class SchemaSource private constructor(
    val stream: InputStream? = null,
    val reader: Reader? = null,
    val baseUri: URI? = null,
    val version: JsonSchemaVersion = DRAFT07
) {
    @JvmOverloads
    constructor(
        stream: InputStream,
        version: JsonSchemaVersion = DRAFT07
    ) : this(stream, null, null, version)

    @JvmOverloads
    constructor(
        reader: Reader,
        version: JsonSchemaVersion = DRAFT07
    ) : this(null, reader, null, version)

    @JvmOverloads
    constructor(
        stream: InputStream,
        baseUri: URI,
        version: JsonSchemaVersion = DRAFT07
    ) : this(stream, null, baseUri, version)

    @JvmOverloads
    constructor(
        reader: Reader,
        baseUri: URI,
        version: JsonSchemaVersion = DRAFT07
    ) : this(null, reader, baseUri, version)

    init {
        if (stream == null && reader == null)
            throw IllegalArgumentException("Either stream or reader needs a value, both cannot be null")
    }
}

class SchemaSources(val sources: List<SchemaSource>) : List<SchemaSource> by sources {
    @JvmOverloads
    constructor(
        version: JsonSchemaVersion = DRAFT07,
        vararg streams: InputStream
    ) : this(streams.map { SchemaSource(stream = it, version = version) })

    @JvmOverloads
    constructor(
        version: JsonSchemaVersion = DRAFT07,
        vararg readers: Reader
    ) : this(readers.map { SchemaSource(reader = it, version = version) })

    constructor(vararg sources: SchemaSource) : this(sources.toList())
}