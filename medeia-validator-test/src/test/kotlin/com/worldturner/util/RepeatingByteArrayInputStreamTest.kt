package com.worldturner.util

import org.junit.Assert.assertArrayEquals
import kotlin.test.Test

class RepeatingByteArrayInputStreamTest {
    val stringData = "abcdefg"
    val data = stringData.toByteArray(Charsets.UTF_8)
    val repeats = 512351
    val stream = RepeatingByteArrayInputStream(data, repeats)

    @Test
    fun `Correct repeated data`() {
        val streamBytes = stream.readBytes()
        assertArrayEquals(stringData.repeat(repeats).toByteArray(Charsets.UTF_8), streamBytes)
    }
}