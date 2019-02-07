package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import java.nio.file.Paths

val metaSchemaPath = Paths.get("src/main/resources/meta-schemas/schema-draft04.json")

fun main() {
    val warmups = 10
    val iterations = 15000
    val allTests = listOf(
        {
            EveritPerformanceTest(metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming(metaSchemaPath) }
                println("Everit:   " + test.runWithTiming(metaSchemaPath).let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaGsonPerformanceTest(metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming(metaSchemaPath) }
                println("Medeia-G: " + test.runWithTiming(metaSchemaPath).let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaJacksonPerformanceTest(metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming(metaSchemaPath) }
                println("Medeia-J: " + test.runWithTiming(metaSchemaPath).let { "%5.4f".format(it) })
            }
        },
        {
            JsonNodeValidatorPerformanceTest(metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming(metaSchemaPath) }
                println("JsonNode: " + test.runWithTiming(metaSchemaPath).let { "%5.4f".format(it) })
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