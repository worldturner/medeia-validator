package com.worldturner.medeia.api

import org.hamcrest.core.IsEqual
import org.junit.Assert.assertThat
import org.junit.Test

class FailedValidationResultTest {
    @Test
    fun `toString() should indent and output correctly`() {
        val failed = FailedValidationResult(
            rule = "rule1",
            property = "prop2",
            message = "Invalid",
            location = "1:23",
            details = listOf(
                FailedValidationResult(
                    rule = "rule2",
                    message = "Also invalid",
                    location = "5:1"
                ),
                FailedValidationResult(
                    rule = "rule3",
                    message = "Invalid too",
                    location = "6:25"
                )
            )
        )
        val expected = """
Validation Failure
------------------
Rule:     rule1
Property: prop2
Message:  Invalid
Location: 1:23
Details:
    Rule:     rule2
    Message:  Also invalid
    Location: 5:1
    -----
    Rule:     rule3
    Message:  Invalid too
    Location: 6:25
    -----
""".trimStart()
        assertThat(failed.toString(), IsEqual(expected))
    }
}