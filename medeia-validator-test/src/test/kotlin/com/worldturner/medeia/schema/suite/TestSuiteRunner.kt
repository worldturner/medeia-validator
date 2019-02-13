package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.Schema
import com.worldturner.medeia.schema.model.SchemaTest
import com.worldturner.medeia.schema.model.SchemaTestType
import com.worldturner.medeia.schema.model.SchemaWithBaseUri
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
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
    val jsonSchemaType: MapperType = JsonSchemaDraft07Type,
    val schemaTestType: MapperType = SchemaTestType
) {

    val parserLibrary = JsonParserLibrary.JACKSON

    val tests = run {
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
                                ArrayType(schemaTestType),
                                Files.newInputStream(it),
                                parserLibrary
                            ) as List<SchemaTest>
                        tests.map { test -> test.copy(path = it) }.stream()
                    } catch (e: Exception) {
                        throw Exception("Error in file $it", e)
                    }
//                        mapper.readValue<List<SchemaTest>>(it.toUri().toURL(),
//                                object : TypeReference<List<SchemaTest>>() {})
//                                .map { test -> test.copy(path = it) }.stream()
                } else {
                    Stream.empty()
                }
            else
                loadTests(it, filter).stream()
        }.collect(Collectors.toList())

    fun loadMetaSchema() =
        parse(jsonSchemaType, metaSchemaUrl.openStream(), parserLibrary) as JsonSchema

    fun loadRemoteSchemas(path: Path): Set<Schema> =
        Files.list(path).flatMap { child ->
            if (Files.isRegularFile(child))
                if (child.fileName.toString().endsWith(".json")) {
                    println("Remote Schema: $child")
                    val relativePath = remoteSchemasPath.relativize(child)
                    val baseUri = remoteSchemasBaseUri.resolve(relativePath.toString())
                    val schema =
                        parse(jsonSchemaType, Files.newInputStream(child), parserLibrary) as JsonSchema
                    Stream.of(schema.let { SchemaWithBaseUri(baseUri, it) })
                } else {
                    Stream.empty()
                }
            else
                loadRemoteSchemas(child).stream()
        }.collect(Collectors.toSet())

    fun testStreamingGenerator(library: JsonParserLibrary) = tests.flatMap { it.testStreamingGenerator(library) }
    fun testStreamingParser(library: JsonParserLibrary) = tests.flatMap { it.testStreamingParser(library) }
}
