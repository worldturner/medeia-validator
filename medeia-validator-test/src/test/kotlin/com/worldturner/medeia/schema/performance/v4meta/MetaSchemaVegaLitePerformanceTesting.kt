package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import java.nio.file.Paths

val vegaLiteSchemaPath = Paths.get("Performance-Suite/vega-lite/vega-lite-schema-v4-cleaned-up.json")

fun main() {
    val warmups = 20
    val iterations = 100
    EveritPerformanceTest(metaSchemaPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteSchemaPath) }
        println("Everit:   " + test.runWithTiming(vegaLiteSchemaPath).let { "%6.4f".format(it) })
    }
    System.gc()
    MedeiaGsonPerformanceTest(metaSchemaPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteSchemaPath) }
        println("Medeia-G: " + test.runWithTiming(vegaLiteSchemaPath).let { "%6.4f".format(it) })
    }
    System.gc()
    MedeiaJacksonPerformanceTest(metaSchemaPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteSchemaPath) }
        println("Medeia-J: " + test.runWithTiming(vegaLiteSchemaPath).let { "%6.4f".format(it) })
    }
    System.gc()
    JsonNodeValidatorPerformanceTest(metaSchemaPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteSchemaPath) }
        println("JsonNode: " + test.runWithTiming(vegaLiteSchemaPath).let { "%6.4f".format(it) })
    }
    System.gc()
}