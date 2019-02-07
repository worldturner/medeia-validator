package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.schema.TestSuiteRunner
import com.worldturner.medeia.schema.model.TestResult
import com.worldturner.medeia.testing.support.JsonParserLibrary
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import kotlin.test.assertEquals

abstract class RegressionTests {
    abstract val runner: TestSuiteRunner
    abstract val minimumTestCount: Int

    @Test
    fun `Run streaming generator test with Jackson()`() {
        checkResults(runner.testStreamingGenerator(JsonParserLibrary.JACKSON))
    }

    @Test
    fun `Run streaming generator test with Gson()`() {
        checkResults(runner.testStreamingGenerator(JsonParserLibrary.GSON))
    }

    @Test
    fun `Run streaming parser test with Jackson()`() {
        checkResults(runner.testStreamingParser(JsonParserLibrary.JACKSON))
    }

    @Test
    fun `Run streaming parser test with Gson()`() {
        checkResults(runner.testStreamingParser(JsonParserLibrary.GSON))
    }

    private fun checkResults(result: List<TestResult>) {
        assertThat(
            "Number of tests executed must be greater than or equal to $minimumTestCount",
            result.size, Matchers.greaterThanOrEqualTo(minimumTestCount)
        )
        assertEquals(
            0,
            result.count { !it.testSucceeded },
            "Count of failed non-optional tests should be 0"
        )
    }
}