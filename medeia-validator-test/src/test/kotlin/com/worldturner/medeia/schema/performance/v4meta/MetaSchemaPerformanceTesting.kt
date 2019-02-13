package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import java.nio.file.Paths

val metaSchemaPath =
    Paths.get("medeia-validator-core/src/main/resources/meta-schemas/schema-draft04.json").toAbsolutePath()

fun main() {
    val warmups = 10
    val iterations = 10000
    val allTests = listOf(
        {
            EveritPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                println("Everit:   " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaGsonPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                println("Medeia-G: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaJacksonPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                println("Medeia-J: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            JsonNodeValidatorPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                println("JsonNode: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        }
    )

    val tests = allTests.subList(0, 3).shuffled()
//    val tests = allTests.subList(2, 3).shuffled()
    tests.forEach {
        it()
        System.gc()
    }
}