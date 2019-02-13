package com.worldturner.util

import org.junit.Test

class JsonValidatorTest {
    @Test
    fun `Simple json should validate`() {
        JsonValidator(
            """
            { "hello" : "world", "number" : 12.34e56, "list": ["a", false, true, null] }
            """.trimIndent()
        )
    }

    @Test
    fun `Valid numbers`() {
        JsonValidator(
            """
            [0, 10, -0, -10, 0.1, -0.1, 0.123, -0.123, 99.99e3, 99.99e-3, 99e3, 99e-3  ]
            """.trimIndent()
        )
    }

    @Test(expected = JsonParseException::class)
    fun `Invalid number #1`() {
        JsonValidator("01")
    }

    @Test(expected = JsonParseException::class)
    fun `Invalid number #2`() {
        JsonValidator(".5")
    }

    @Test(expected = JsonParseException::class)
    fun `Invalid number #3`() {
        JsonValidator("24f")
    }
}