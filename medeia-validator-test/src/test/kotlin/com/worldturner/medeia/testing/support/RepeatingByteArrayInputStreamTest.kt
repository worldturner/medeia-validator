package com.worldturner.medeia.testing.support

import org.junit.Assert
import org.junit.Test

class RepeatingByteArrayInputStreamTest {
    val stringData = "abcdefg"
    val data = stringData.toByteArray(Charsets.UTF_8)
    val repeats = 512351
    val stream = RepeatingByteArrayInputStream(data, repeats)

    @Test
    fun `Correct repeated data`() {
        val streamBytes = stream.readBytes()
        Assert.assertArrayEquals(stringData.repeat(repeats).toByteArray(Charsets.UTF_8), streamBytes)
    }
}