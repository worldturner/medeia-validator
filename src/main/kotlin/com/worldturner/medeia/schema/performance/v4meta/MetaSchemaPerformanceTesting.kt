package com.worldturner.medeia.schema.performance.v4meta

import com.worldturner.medeia.api.InputSource
import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.MetaSchemaSource
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.schema.performance.EveritPerformanceTest
import com.worldturner.medeia.schema.performance.JsonNodeValidatorPerformanceTest
import com.worldturner.medeia.schema.performance.JustifyPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaGsonPerformanceTest
import com.worldturner.medeia.schema.performance.MedeiaJacksonPerformanceTest
import com.worldturner.medeia.schema.performance.PerformanceTest
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

fun supportsVersion(
    test: KClass<out PerformanceTest>,
    version: JsonSchemaVersion
): Boolean {
    val supportsValue =
        test.companionObject?.java?.getMethod("getSupports")?.invoke(test.companionObjectInstance) as Set<*>?
    return supportsValue?.let { version in it } ?: false
}

fun <T : PerformanceTest> createTestInstance(
    test: KClass<T>,
    schemaSource: SchemaSource,
    dataSource: InputSource,
    iterations: Int
): T = test.primaryConstructor!!.call(schemaSource, dataSource, iterations)

fun <T : PerformanceTest> createTest(
    instance: T,
    warmups: Int
): () -> Unit {
    return {
        val name = instance::class.simpleName!!.substringBefore("PerformanceTest")
        (1..warmups).forEach { instance.runWithTiming() }
        println("$name: " + instance.runWithTiming().let { "%5.4f".format(it) })
    }
}

fun main(vararg args: String) {
    if (args.size != 2) {
        error("Please provide 2 arguments: schema version (draft04/draft06/draft07) and test index")
    }
    val schemaVersion = JsonSchemaVersion.valueOf(args[0].toUpperCase())
    val testIndex = args[1].toInt()
    val metaSchemaSource = MetaSchemaSource.forVersion(schemaVersion)

    val warmups = 20
    val iterations = 10000
    val allTests = listOf(
        JustifyPerformanceTest::class,
        MedeiaJacksonPerformanceTest::class,
        MedeiaGsonPerformanceTest::class,
        EveritPerformanceTest::class,
        JsonNodeValidatorPerformanceTest::class
    ).filter { supportsVersion(it, schemaVersion) }
        .map {
            createTestInstance(it, metaSchemaSource, metaSchemaSource.input, iterations)
        }
        .map {
            createTest(it, warmups)
        }

    val test = allTests[testIndex]
    test()
}