package com.worldturner.medeia.schema.schemastore

import com.worldturner.medeia.api.PathSchemaSource
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.util.iterate
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors

val schemastorePath = Paths.get("../../schemastore/src/schemas/json/")

class LoadSchemaStoreSchemasTest {
    val api = MedeiaJacksonApi()

    val combine = listOf(
        "grunt-.*",
        "jsbeautify.*.json",
        "schema-org-.*.json"
    ).map { Regex(it) }

    val exclude = listOf(
        "nodemon.json", // has broken regex in pattern
        "cryproj.*\\.json", // has broken $schema version
        "webextension.json", // has broken regex in pattern
//        "schema-org-contact-point.json", // has missing $ref http://json.schemastore.org/schema-org-thing
        "template.json", // has broken regex in pattern ^{?[0-9A-Fa-f]{8}[-]?([0-9A-Fa-f]{4}[-]?){3}[0-9A-Fa-f]{12}}?$
//        "schema-org-.*.json",
        // "ocelot.json", // Runs into undefined behaviour because it defines ids with json pointer fragments
        "nlu.json", // is not a schema
        // "datalogic-scan2deploy-android.json", // Runs into undefined behaviour because it defines ids with json pointer fragments
//        "grunt-.*", // Needs to be parsed together
        "sourcemap-v3.json", // Invalid UTF-8
        "vs-2017.3.host.json", // has broken regex in pattern ^{?[0-9A-Fa-f]{8}[-]?([0-9A-Fa-f]{4}[-]?){3}[0-9A-Fa-f]{12}}?$
        "licenses.1.json", // doesn't specify schema version
        "cirrus.json", // Undefined $refs
        "vega-lite.json", // Illegal characters in fragment
        "asmdef.json", // doesn't specify schema version
//        "jsbeautify.*.json", // Needs to be parsed together
        "vega.json" // Probably Needs to be parsed together
    )

    private fun excluded(p: Path): Boolean {
        val filename = p.fileName.toString()
        return exclude.any { Regex(it).matches(filename) }
    }

    @Test
    fun loadSchemas() {
        println(schemastorePath.toAbsolutePath())
        val sorted = Files.list(schemastorePath)
            .filter { it.fileName.toString().endsWith(".json") && !excluded(it) }
            .sorted { a: Path, b: Path -> a.toString().compareTo(b.toString()) }
            .collect(Collectors.toList()).toMutableList()

        val combined = mutableListOf<List<Path>>()
        combine.forEach { combine ->
            println("Checking $combine")
            val combinedPaths = mutableListOf<Path>()
            sorted.iterate { path, iterator ->
                if (combine.matches(path.fileName.toString())) {
                    println("Combining $path")
                    combinedPaths.add(path)
                    iterator.remove()
                }
            }
            if (combinedPaths.isNotEmpty()) combined += combinedPaths
        }
        sorted.forEach { combined.add(listOf(it)) }
        combined.forEach { loadSchemas(it) }
    }

    fun loadSchemas(schemaPaths: List<Path>) {
        println("Loading schema ${schemaPaths.joinToString()}")
        try {
            api.loadSchemas(schemaPaths.map { PathSchemaSource(it) })
        } catch (e: Exception) {
            println("Failure: $e")
        }
    }
}