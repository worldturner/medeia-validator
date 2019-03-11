package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.api.MetaSchemaSource
import com.worldturner.medeia.api.PathInputSource
import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import java.nio.file.Paths

private val metaSchemaSource = MetaSchemaSource.DRAFT04
val vegaLiteSource = PathInputSource(Paths.get("Performance-Suite/vega-lite/vega-lite-schema-v4-cleaned-up.json"))

fun main() {
    val warmups = 20
    val iterations = 100
    EveritPerformanceTest(metaSchemaSource, vegaLiteSource, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("Everit:   " + test.runWithTiming().let { "%6.4f".format(it) })
    }
    System.gc()
    MedeiaGsonPerformanceTest(metaSchemaSource, vegaLiteSource, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("Medeia-G: " + test.runWithTiming().let { "%6.4f".format(it) })
    }
    System.gc()
    MedeiaJacksonPerformanceTest(metaSchemaSource, vegaLiteSource, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("Medeia-J: " + test.runWithTiming().let { "%6.4f".format(it) })
    }
    System.gc()
    JsonNodeValidatorPerformanceTest(metaSchemaSource, vegaLiteSource, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("JsonNode: " + test.runWithTiming().let { "%6.4f".format(it) })
    }
    System.gc()
}