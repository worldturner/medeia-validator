package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import java.nio.file.Paths

val metaSchemaPath =
    Paths.get("medeia-validator-core/src/main/resources/meta-schemas/schema-draft04.json").toAbsolutePath()

fun gc() {
    (1..10).forEach { System.gc(); Thread.sleep(50) }
    Thread.sleep(2000)
}
fun main() {
    val warmups = 20
    val iterations = 10000
    val allTests = listOf(
        {
            MedeiaJacksonPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Medeia-J: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaGsonPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Medeia-G: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            EveritPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Everit:   " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            JsonNodeValidatorPerformanceTest(metaSchemaPath, metaSchemaPath, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("JsonNode: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        }
    )

//    val tests = allTests

    val tests = allTests.subList(2, 3)

    tests.forEach {
        it()
    }
}