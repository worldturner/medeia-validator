package com.worldturner.medeia.api

import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT07
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

enum class JsonSchemaVersion(internal val mapperType: MapperType) {
    DRAFT04(JsonSchemaDraft04Type), DRAFT07(JsonSchemaDraft07Type)
}

enum class InputPreference { STREAM, READER }

interface SchemaSource {
    val inputPreference: InputPreference
    val stream: InputStream get() = throw UnsupportedOperationException()
    val reader: Reader get() = InputStreamReader(stream, Charsets.UTF_8)
    val baseUri: URI?
    val version: JsonSchemaVersion
}

class StreamSchemaSource(
    override val stream: InputStream,
    override val baseUri: URI? = null,
    override val version: JsonSchemaVersion = DRAFT07
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM
}

class ReaderSchemaSource(
    override val reader: Reader,
    override val baseUri: URI? = null,
    override val version: JsonSchemaVersion = DRAFT07
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.READER
}

class PathSchemaSource(
    val path: Path,
    override val baseUri: URI? = null,
    override val version: JsonSchemaVersion = DRAFT07
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = Files.newInputStream(path)
}

class UrlSchemaSource(
    val url: URL,
    override val baseUri: URI? = null,
    override val version: JsonSchemaVersion = DRAFT07
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = url.openStream()
}

class StringSchemaSource(
    val string: String,
    override val baseUri: URI? = null,
    override val version: JsonSchemaVersion = DRAFT07
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.READER

    override val reader: Reader
        get() = StringReader(string)
}

object SchemaSources {
    @JvmStatic
    fun create(
        version: JsonSchemaVersion,
        vararg streams: InputStream
    ) = streams.map { StreamSchemaSource(stream = it, version = version) }

    @JvmStatic
    fun create(
        version: JsonSchemaVersion,
        vararg readers: Reader
    ) = readers.map { ReaderSchemaSource(reader = it, version = version) }
}
