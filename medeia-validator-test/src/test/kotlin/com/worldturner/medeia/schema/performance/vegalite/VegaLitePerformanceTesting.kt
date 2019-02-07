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
    MedeiaJacksonPerformanceTest(vegaLiteSchemaPath, iterations, schemaType = JsonSchemaDraft04Type).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteDataPath) }
        println("Medeia-J: " + test.runWithTiming(vegaLiteDataPath).let { "%5.4f".format(it) })
    }
    System.gc()
    MedeiaGsonPerformanceTest(vegaLiteSchemaPath, iterations, schemaType = JsonSchemaDraft04Type).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteDataPath) }
        println("Medeia-G: " + test.runWithTiming(vegaLiteDataPath).let { "%5.4f".format(it) })
    }
    System.gc()
    EveritPerformanceTest(vegaLiteOrigSchemaPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteDataPath) }
        println("Everit:   " + test.runWithTiming(vegaLiteDataPath).let { "%5.4f".format(it) })
    }
    System.gc()
    JsonNodeValidatorPerformanceTest(vegaLiteOrigSchemaPath, iterations).let { test ->
        (1..warmups).forEach { test.runWithTiming(vegaLiteDataPath) }
        println("JsonNode: " + test.runWithTiming(vegaLiteDataPath).let { "%5.4f".format(it) })
    }
    System.gc()
}