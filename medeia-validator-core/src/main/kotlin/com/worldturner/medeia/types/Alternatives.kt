package com.worldturner.medeia.types

open class Alternatives<A, B>(val a: A? = null, val b: B? = null) {
    init {
        if (!((a == null) xor (b == null))) {
            throw KotlinNullPointerException("Either a or b has to be non-null, not both or neither")
        }
    }
}