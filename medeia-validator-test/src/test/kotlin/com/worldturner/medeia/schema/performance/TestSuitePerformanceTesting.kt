package com.worldturner.medeia.schema.performance

import com.worldturner.medeia.schema.TestSuiteRunner
import com.worldturner.medeia.schema.suite.DRAFT07_RUNNER
import com.worldturner.medeia.testing.support.JsonParserLibrary

class TestSuitePerformanceTesting {
    var warmups = 10
    val times = 15
    val iterations = 200

    val runner = DRAFT07_RUNNER

    val tests = listOf<(TestSuiteRunner) -> Unit>(
        { runner -> runner.testStreamingGenerator(JsonParserLibrary.JACKSON) },
        { runner -> runner.testStreamingGenerator(JsonParserLibrary.GSON) },
        { runner -> runner.testStreamingParser(JsonParserLibrary.JACKSON) },
        { runner -> runner.testStreamingParser(JsonParserLibrary.GSON) }
    )

    val testNames = listOf("Generator-J", "Generator-G", "Parser-J", "Parser-G")

    fun runPerformance() {
        println("Test names: $testNames")
        val warmups = runAll(warmups)
        println("Warmups: ${average(warmups).map { "%5.4f".format(it) }}")
        (1..2).forEach {
            val results = runAll(times)
            println("Results: ${average(results).map { "%5.4f".format(it) }}")
        }
    }

    fun average(timings: List<List<Double>>) =
        timings.map { it.average() }

    fun runAll(times: Int) =
        tests.map { test ->
            (0..times).map {
                timeIterations(runner, test)
            }
        }

    fun timeIterations(runner: TestSuiteRunner, test: (TestSuiteRunner) -> Unit): Double {
        val start = System.nanoTime()
        for (i in 0..iterations) {
            test(runner)
        }
        val end = System.nanoTime()
        return (end - start).toDouble() / iterations / 1_000_000.0
    }
}

fun main() {
    TestSuitePerformanceTesting().runPerformance()
}