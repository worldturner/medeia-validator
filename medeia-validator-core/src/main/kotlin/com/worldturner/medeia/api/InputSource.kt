package com.worldturner.medeia.api

import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader
import java.io.StringReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

enum class InputPreference { STREAM, READER }

/**
 * A source can always be accessed as a reader, but when its preference is READER,
 * it cannot be accessed as a stream. Sometimes, optimized handles can process
 * an InputStream faster than a Reader (Jackson has a fast JSON parser than handles
 * the UTF8 encoding itself)
 */
interface InputSource {
    val preference: InputPreference
    val stream: InputStream get() = throw UnsupportedOperationException()
    val reader: Reader get() = InputStreamReader(stream, Charsets.UTF_8)
    val name: String?
}

class StreamInputSource @JvmOverloads constructor(
    override val stream: InputStream,
    override val name: String? = null
) : InputSource {
    override val preference: InputPreference
        get() = InputPreference.STREAM

    override fun toString(): String {
        val builder = StringBuilder("StreamInputSource(")
        name?.let { builder.append(it) }
        return builder.append(")").toString()
    }
}

class ReaderInputSource @JvmOverloads constructor(
    override val reader: Reader,
    override val name: String? = null
) : InputSource {
    override val preference: InputPreference
        get() = InputPreference.READER

    override fun toString(): String {
        val builder = StringBuilder("ReaderInputSource(")
        name?.let { builder.append(it) }
        return builder.append(")").toString()
    }
}

class PathInputSource(
    val path: Path
) : InputSource {
    override val preference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = Files.newInputStream(path)
    override val name: String = path.toString()

    override fun toString(): String = "PathInputSource($path)"
}

class UrlInputSource(
    val url: URL
) : InputSource {
    override val preference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = url.openStream()
    override val name: String = url.toString()

    override fun toString(): String = "UrlInputSource($url)"
}

class StringInputSource @JvmOverloads constructor(
    val string: String,
    override val name: String? = null
) : InputSource {
    override val preference: InputPreference
        get() = InputPreference.READER
    override val reader: Reader
        get() = StringReader(string)

    override fun toString(): String {
        val builder = StringBuilder("StringInputSource(")
        builder.append(name ?: string.substring(0, Math.min(string.length, 40)))
        return builder.append(")").toString()
    }
}

class MetaSchemaInputSource private constructor(
    val version: JsonSchemaVersion
) : InputSource {
    override val preference: InputPreference
        get() = InputPreference.STREAM
    override val stream: InputStream
        get() = this::class.java.getResourceAsStream(version.toMetaSchemaResource())
    override val name: String?
        get() = version.toMetaSchemaResource().substringAfterLast('/')

    override fun toString(): String = "MetaSchemaInputSource(version=$version)"

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

        val DRAFT04 = MetaSchemaInputSource(JsonSchemaVersion.DRAFT04)
        val DRAFT06 = MetaSchemaInputSource(JsonSchemaVersion.DRAFT06)
        val DRAFT07 = MetaSchemaInputSource(JsonSchemaVersion.DRAFT07)
        fun forVersion(version: JsonSchemaVersion) =
            when (version) {
                JsonSchemaVersion.DRAFT04 -> DRAFT04
                JsonSchemaVersion.DRAFT06 -> DRAFT06
                JsonSchemaVersion.DRAFT07 -> DRAFT07
            }
    }
}