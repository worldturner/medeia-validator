package com.worldturner.util

import kotlin.reflect.KMutableProperty0

inline fun <T> KMutableProperty0<T?>.withValue(
    assignValue: T?,
    action: () -> Unit
) {
    val savedValue = this.get()
    this.set(assignValue)
    try {
        action()
    } finally {
        this.set(savedValue)
    }
}

fun <T> KMutableProperty0<T?>.getAndClear(): T? {
    val value = this.get()
    this.set(null)
    return value
}
