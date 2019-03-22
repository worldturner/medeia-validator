package com.worldturner.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionsTest {
    @Test
    fun `List-repeat should repeat source list n times`() {
        assertEquals(listOf(1, 2, 1, 2, 1, 2), listOf(1, 2).repeat(3))
        assertEquals(listOf(), listOf(1, 2).repeat(0))
        assertEquals(listOf(), listOf<Int>().repeat(100))
    }

    @Test
    fun `Iterate method for MutableCollection`() {
        val c = mutableListOf("a", "b", "cc", "d")
        c.iterate { e, i ->
            if (e.length == 2)
                i.remove()
        }
        assertEquals(listOf("a", "b", "d"), c)
    }
}