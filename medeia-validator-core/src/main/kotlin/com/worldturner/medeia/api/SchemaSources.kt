package com.worldturner.medeia.api

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
    DRAFT04(JsonSchemaDraft04Type), DRAFT06(JsonSchemaDraft07Type), DRAFT07(JsonSchemaDraft07Type)
}

enum class InputPreference { STREAM, READER }

interface SchemaSource {
    val inputPreference: InputPreference
    val stream: InputStream get() = throw UnsupportedOperationException()
    val reader: Reader get() = InputStreamReader(stream, Charsets.UTF_8)
    val baseUri: URI?
    val version: JsonSchemaVersion?
}

class StreamSchemaSource @JvmOverloads constructor(
    override val stream: InputStream,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM

    override fun toString(): String {
        val builder = StringBuilder("StreamSchemaSource(")
        builder.append(
            listOfNotNull(
                baseUri?.let { "baseUri=$baseUri" },
                version?.let { "version=$version" }).joinToString()
        )
        return builder.append(")").toString()
    }
}

class ReaderSchemaSource @JvmOverloads constructor(
    override val reader: Reader,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.READER

    override fun toString(): String {
        val builder = StringBuilder("ReaderSchemaSource(")
        builder.append(
            listOfNotNull(
                baseUri?.let { "baseUri=$baseUri" },
                version?.let { "version=$version" }).joinToString()
        )
        return builder.append(")").toString()
    }
}

class PathSchemaSource @JvmOverloads constructor(
    val path: Path,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = Files.newInputStream(path)

    override fun toString(): String {
        val builder = StringBuilder("PathSchemaSource(")
        builder.append(
            listOfNotNull(
                "path=$path",
                baseUri?.let { "baseUri=$baseUri" },
                version?.let { "version=$version" }).joinToString()
        )
        return builder.append(")").toString()
    }
}

class UrlSchemaSource @JvmOverloads constructor(
    val url: URL,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = url.openStream()

    override fun toString(): String {
        val builder = StringBuilder("UrlSchemaSource(")
        builder.append(
            listOfNotNull(
                "url=$url",
                baseUri?.let { "baseUri=$baseUri" },
                version?.let { "version=$version" }).joinToString()
        )
        return builder.append(")").toString()
    }
}

class MetaSchemaSource private constructor(override val version: JsonSchemaVersion) : SchemaSource {
    override val baseUri: URI? get() = null
    override val inputPreference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = this::class.java.getResourceAsStream(version.toMetaSchemaResource())

    override fun toString(): String = "MetaSchemaSource(version=$version)"

    companion object {
        private const val RESOURCE_SCHEMA_DRAFT04 = "/meta-schemas/schema-draft04.json"
        private const val RESOURCE_SCHEMA_DRAFT06 = "/meta-schemas/schema-draft06.json"
        private const val RESOURCE_SCHEMA_DRAFT07 = "/meta-schemas/schema-draft07.json"
        private fun JsonSchemaVersion.toMetaSchemaResource(): String =
            when (this) {
                JsonSchemaVersion.DRAFT04 -> RESOURCE_SCHEMA_DRAFT04
                JsonSchemaVersion.DRAFT06 -> RESOURCE_SCHEMA_DRAFT06
                JsonSchemaVersion.DRAFT07 -> RESOURCE_SCHEMA_DRAFT07
            }

        val DRAFT04 = MetaSchemaSource(JsonSchemaVersion.DRAFT04)
        val DRAFT06 = MetaSchemaSource(JsonSchemaVersion.DRAFT06)
        val DRAFT07 = MetaSchemaSource(JsonSchemaVersion.DRAFT07)
    }
}

class StringSchemaSource @JvmOverloads constructor(
    val string: String,
    override val version: JsonSchemaVersion? = null,
    override val baseUri: URI? = null
) : SchemaSource {
    override val inputPreference: InputPreference
        get() = InputPreference.READER

    override val reader: Reader
        get() = StringReader(string)

    override fun toString(): String {
        val builder = StringBuilder("StringSchemaSource(")
        builder.append(
            listOfNotNull(
                "string=${string.substring(0, Math.min(string.length, 40))}",
                baseUri?.let { "baseUri=$baseUri" },
                version?.let { "version=$version" }).joinToString()
        )
        return builder.append(")").toString()
    }
}
