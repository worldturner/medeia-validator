package com.worldturner.medeia.schema.performance

/**
 * Returns the time it takes to perform the action in milliseconds, with nanosecond precision.
 */
inline fun timing(iterations: Int = 1, action: () -> Unit): Double {
    val start = System.nanoTime()
    for (i in 0 until iterations) {
        action()
    }
    val end = System.nanoTime()
    return (end - start).toDouble() / iterations / 1_000_000.0
}