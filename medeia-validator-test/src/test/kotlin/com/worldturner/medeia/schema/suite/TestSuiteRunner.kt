package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.PathSchemaSource
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.api.UrlSchemaSource
import com.worldturner.medeia.api.gson.MedeiaGsonApi
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.schema.model.SchemaTest
import com.worldturner.medeia.schema.model.SchemaTestType
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import java.util.stream.Stream

data class TestSuiteRunner(
    val testsPaths: List<Path>,
    val remoteSchemasPath: Path,
    val metaSchemaUrl: URL,
    val remoteSchemasBaseUri: URI,
    val optional: Boolean = false,
    val filter: (Path) -> Boolean = { true },
    val version: JsonSchemaVersion
) {

    val parserLibrary = JsonParserLibrary.JACKSON

    val tests
        get() = run {
            val remoteSchemas = loadRemoteSchemas(remoteSchemasPath) + loadMetaSchema()
            loadTests {
                filter(it) && (optional xor !it.contains(Paths.get("optional")))
            }.map { it.withRemotes(remoteSchemas) }
        }

    fun loadTests(filter: (Path) -> Boolean = { true }) =
        testsPaths.flatMap { loadTests(it, filter) }

    internal fun loadTests(path: Path, filter: (Path) -> Boolean): List<SchemaTest> =
        Files.list(path).flatMap {
            if (Files.isRegularFile(it))
                if (it.fileName.toString().endsWith(".json") && filter(it)) {
                    System.err.println("File: $it")
                    try {
                        @Suppress("UNCHECKED_CAST")
                        val tests =
                            parse(
                                ArrayType(SchemaTestType),
                                Files.newInputStream(it),
                                parserLibrary
                            ) as List<SchemaTest>
                        tests.map { test -> test.copy(path = it) }.stream()
                    } catch (e: Exception) {
                        throw Exception("Error in file $it", e)
                    }
                } else {
                    Stream.empty()
                }
            else
                loadTests(it, filter).stream()
        }.collect(Collectors.toList())

    fun loadMetaSchema(): SchemaSource = UrlSchemaSource(metaSchemaUrl, version = version)

    fun loadRemoteSchemas(path: Path): Set<SchemaSource> =
        Files.list(path).flatMap { child ->
            if (Files.isRegularFile(child))
                if (child.fileName.toString().endsWith(".json")) {
                    println("Remote Schema: $child")
                    val relativePath = remoteSchemasPath.relativize(child)
                    val baseUri = remoteSchemasBaseUri.resolve(relativePath.toString())
                    Stream.of(PathSchemaSource(child, baseUri, version))
                } else {
                    Stream.empty()
                }
            else
                loadRemoteSchemas(child).stream()
        }.collect(Collectors.toSet())

    val medeiaJacksonApi = MedeiaJacksonApi()
    val medeiaGsonApi = MedeiaGsonApi()

    fun medeiaApiBase(library: JsonParserLibrary) =
        when (library) {
            JsonParserLibrary.JACKSON -> medeiaJacksonApi
            JsonParserLibrary.GSON -> medeiaGsonApi
        }

    fun testStreamingGenerator(library: JsonParserLibrary) =
        tests.flatMap { it.testStreamingGenerator(medeiaApiBase(library), version, library) }

    fun testStreamingParser(library: JsonParserLibrary) =
        tests.flatMap { it.testStreamingParser(medeiaApiBase(library), version, library) }
}
