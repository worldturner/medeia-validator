package com.worldturner.medeia.format

import org.junit.Test
import kotlin.test.assertEquals

class IdnHostnameTest {
    @Test
    fun wikipediaExample() {
        val idn = "Bücher.example"
        val ldh = "xn--Bcher-kva.example"
        assertEquals(ldh, idn.idnToLdhHostname())
    }

    @Test
    fun wikipediaExampleModified() {
        val idn = "Bücher.example."
        val ldh = "xn--Bcher-kva.example."
        assertEquals(ldh, idn.idnToLdhHostname())
    }
}