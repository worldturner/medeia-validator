package com.worldturner.medeia.api

import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
import java.io.InputStream
import java.io.Reader
import java.net.URI
import java.net.URL
import java.nio.file.Path

enum class JsonSchemaVersion(internal val mapperType: MapperType) {
    DRAFT04(JsonSchemaDraft04Type), DRAFT06(JsonSchemaDraft07Type), DRAFT07(JsonSchemaDraft07Type)
}

interface SchemaSource {
    val input: InputSource
    val version: JsonSchemaVersion?
    val baseUri: URI?
}

open class InputSourceSchemaSource(
    override val input: InputSource,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : SchemaSource {
    override fun toString(): String {
        val builder = StringBuilder(this::class.simpleName).append('(')
        builder.append(
            listOfNotNull(
                "input=$input",
                baseUri?.let { "baseUri=$baseUri" },
                version?.let { "version=$version" }).joinToString()
        )
        return builder.append(")").toString()
    }
}

class StreamSchemaSource @JvmOverloads constructor(
    val stream: InputStream,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : InputSourceSchemaSource(StreamInputSource(stream), version, baseUri)

class ReaderSchemaSource @JvmOverloads constructor(
    val reader: Reader,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : InputSourceSchemaSource(ReaderInputSource(reader), version, baseUri)

class PathSchemaSource @JvmOverloads constructor(
    val path: Path,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : InputSourceSchemaSource(PathInputSource(path), version, baseUri)

class UrlSchemaSource @JvmOverloads constructor(
    val url: URL,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : InputSourceSchemaSource(UrlInputSource(url), version, baseUri)

class StringSchemaSource @JvmOverloads constructor(
    val string: String,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : InputSourceSchemaSource(StringInputSource(string), version, baseUri)

class MetaSchemaSource private constructor(
    override val version: JsonSchemaVersion
) : InputSourceSchemaSource(MetaSchemaInputSource.forVersion(version), version, null) {

    override fun toString(): String = "MetaSchemaSource(version=$version)"

    companion object {
        val DRAFT04 = MetaSchemaSource(JsonSchemaVersion.DRAFT04)
        val DRAFT06 = MetaSchemaSource(JsonSchemaVersion.DRAFT06)
        val DRAFT07 = MetaSchemaSource(JsonSchemaVersion.DRAFT07)
        fun forVersion(version: JsonSchemaVersion) =
            when (version) {
                JsonSchemaVersion.DRAFT04 -> DRAFT04
                JsonSchemaVersion.DRAFT06 -> DRAFT06
                JsonSchemaVersion.DRAFT07 -> DRAFT07
            }
    }
}