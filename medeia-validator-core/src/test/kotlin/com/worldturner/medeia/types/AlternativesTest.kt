package com.worldturner.medeia.types

import org.junit.Test

class AlternativesTest {
    @Test(expected = Exception::class)
    fun `Alternatives with no alternative should fail construction`() {
        Alternatives<Any, Any>()
    }

    @Test(expected = Exception::class)
    fun `Alternatives with both alternative should fail construction`() {
        Alternatives("foo", 42)
    }

    @Test
    fun `Alternatives with only alternative a is OK`() {
        Alternatives<Any, Any>(a = "foo")
    }

    @Test
    fun `Alternatives with only alternative b is OK`() {
        Alternatives<Any, Any>(b = "bar")
    }
}