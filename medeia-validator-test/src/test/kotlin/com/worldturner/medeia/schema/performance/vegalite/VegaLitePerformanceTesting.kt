package com.worldturner.medeia.schema.performance.vegalite

import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import java.nio.file.Paths

val vegaLiteSchemaPath = Paths.get("Performance-Suite/vega-lite/vega-lite-schema-v4-cleaned-up.json")
// val vegaLiteSchemaPath = Paths.get("Performance-Suite/vega-lite/vega-lite-schema-v6-with-v4-id.json")
val vegaLiteOrigSchemaPath = Paths.get("Performance-Suite/vega-lite/vega-lite-schema-v4.json")
val vegaLiteDataPath = Paths.get("Performance-Suite/vega-lite/interactive_splom.vg.json")

fun main() {
    val warmups = 10
    val iterations = 10
    MedeiaJacksonPerformanceTest(vegaLiteSchemaPath, vegaLiteDataPath, iterations, JsonSchemaDraft04Type).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("Medeia-J: " + test.runWithTiming().let { "%5.4f".format(it) })
    }
    System.gc()
    MedeiaGsonPerformanceTest(vegaLiteSchemaPath, vegaLiteDataPath, iterations, JsonSchemaDraft04Type).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("Medeia-G: " + test.runWithTiming().let { "%5.4f".format(it) })
    }
    System.gc()
    EveritPerformanceTest(vegaLiteOrigSchemaPath, vegaLiteDataPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("Everit:   " + test.runWithTiming().let { "%5.4f".format(it) })
    }
    System.gc()
    JsonNodeValidatorPerformanceTest(vegaLiteOrigSchemaPath, vegaLiteDataPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming() }
        println("JsonNode: " + test.runWithTiming().let { "%5.4f".format(it) })
    }
    System.gc()
}