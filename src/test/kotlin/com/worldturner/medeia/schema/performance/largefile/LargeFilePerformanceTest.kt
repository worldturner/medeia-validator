package com.worldturner.medeia.schema.performance.largefile

import com.worldturner.medeia.api.InputSource
import com.worldturner.medeia.api.MetaSchemaInputSource
import com.worldturner.medeia.api.MetaSchemaSource
import com.worldturner.medeia.api.StringSchemaSource
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.schema.performance.timing
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.nio.file.Files
import java.nio.file.Path

val largeSchema = """
{
    "${'$'}schema": "http://json-schema.org/draft-07/schema#",
    "anyOf": [
        { "${'$'}ref": "http://json-schema.org/draft-04/schema#" },
        { "${'$'}ref": "http://json-schema.org/draft-06/schema#" },
        { "${'$'}ref": "http://json-schema.org/draft-07/schema#" }
    ]
}
"""

class LargeFilePerformanceTest {
    val api = MedeiaJacksonApi()

    fun run(repeats: Int) {
        val path = createLargeFile(repeats)
        println("Size of file: ${Files.size(path) / 1024.0 / 1024.0 / 1024.0} Gib")
        val validator = api.loadSchemas(
            listOf(
                StringSchemaSource(largeSchema),
                MetaSchemaSource.DRAFT04,
                MetaSchemaSource.DRAFT06,
                MetaSchemaSource.DRAFT07
            )
        )
        val duration = timing {
            val input = BufferedInputStream(Files.newInputStream(path))
            input.use {
                val parser = api.decorateJsonParser(validator, api.jsonFactory.createParser(input))
                api.parseAll(parser)
            }
        }
        println("Duration: ${duration.let { "%10.4f".format(it) }}ms")
    }

    fun createLargeFile(
        repeats: Int,
        files: List<InputSource> = listOf(
            MetaSchemaInputSource.DRAFT04, MetaSchemaInputSource.DRAFT06, MetaSchemaInputSource.DRAFT07
        )
    ): Path {
        val path = Files.createTempFile("data", ".json")
        val output = BufferedOutputStream(Files.newOutputStream(path))
        output.use {
            for (i in 0 until repeats) {
                files.forEach { file ->
                    file.stream.use { stream ->
                        stream.copyTo(output)
                    }
                }
            }
        }
        return path
    }
}

fun main() {
    for (i in 0 until 3)
        LargeFilePerformanceTest().run(10000)
    LargeFilePerformanceTest().run(100000)
}