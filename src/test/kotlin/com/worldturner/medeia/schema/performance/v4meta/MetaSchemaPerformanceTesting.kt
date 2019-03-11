package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.api.MetaSchemaSource
import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.JustifyPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest

private val metaSchemaSource = MetaSchemaSource.DRAFT07

fun gc() {
    if (false) {
        (1..10).forEach { System.gc(); Thread.sleep(50) }
        Thread.sleep(2000)
    }
}

fun main() {
    val warmups = 20
    val iterations = 10000
    val allTests = listOf(
        {
            JustifyPerformanceTest(metaSchemaSource, metaSchemaSource.input, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Justify: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaJacksonPerformanceTest(metaSchemaSource, metaSchemaSource.input, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Medeia-J: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            MedeiaGsonPerformanceTest(metaSchemaSource, metaSchemaSource.input, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Medeia-G: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            EveritPerformanceTest(metaSchemaSource, metaSchemaSource.input, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("Everit:   " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        },
        {
            JsonNodeValidatorPerformanceTest(metaSchemaSource, metaSchemaSource.input, iterations).let { test ->
                (1..warmups).forEach { test.runWithTiming() }
                gc()
                println("JsonNode: " + test.runWithTiming().let { "%5.4f".format(it) })
            }
        }
    )

    val tests = allTests

//    val tests = allTests.subList(2, 3)

    tests.forEach {
        it()
    }
}