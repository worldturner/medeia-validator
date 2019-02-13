package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.schema.model.TestResult
import com.worldturner.medeia.testing.support.JsonParserLibrary
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.junit.Test
import kotlin.test.assertEquals

abstract class RegressionTests {
    abstract val runner: TestSuiteRunner
    abstract val minimumTestCount: Int
    open val minimumOptionalTestCount = 1
    open val minimumOptionalSuccessCount = 1

    val optionalRunner: TestSuiteRunner
        get() = runner.copy(optional = true)

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

    // ---
    @Test
    fun `Run optional streaming generator test with Jackson()`() {
        checkResultsOptional(optionalRunner.testStreamingGenerator(JsonParserLibrary.JACKSON))
    }

    @Test
    fun `Run optional streaming generator test with Gson()`() {
        checkResultsOptional(optionalRunner.testStreamingGenerator(JsonParserLibrary.GSON))
    }

    @Test
    fun `Run optional streaming parser test with Jackson()`() {
        checkResultsOptional(optionalRunner.testStreamingParser(JsonParserLibrary.JACKSON))
    }

    @Test
    fun `Run optional streaming parser test with Gson()`() {
        checkResultsOptional(optionalRunner.testStreamingParser(JsonParserLibrary.GSON))
    }

    private fun checkResults(result: List<TestResult>) {
        assertThat(
            "Number of tests executed must be greater than or equal to $minimumTestCount",
            result.size, greaterThanOrEqualTo(minimumTestCount)
        )
        assertEquals(
            0,
            result.count { !it.testSucceeded },
            "Count of failed non-optional tests should be 0"
        )
    }

    private fun checkResultsOptional(result: List<TestResult>) {
        assertThat(
            "Number of tests executed must be greater than or equal to $minimumOptionalTestCount",
            result.size, greaterThanOrEqualTo(minimumOptionalTestCount)
        )
        assertThat(
            "Count of failed non-optional tests should be greater than or equal to $minimumOptionalSuccessCount",
            result.count { !it.testSucceeded },
            greaterThanOrEqualTo(minimumOptionalSuccessCount)
        )
    }
}