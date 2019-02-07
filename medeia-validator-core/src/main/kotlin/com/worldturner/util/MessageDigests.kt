package com.worldturner.util

import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest

fun MessageDigest.updateValue(i: Int) {
    update((i ushr 24).toByte())
    update((i ushr 16).toByte())
    update((i ushr 8).toByte())
    update(i.toByte())
}

fun MessageDigest.updateValue(l: Long) {
    update((l ushr 56).toByte())
    update((l ushr 48).toByte())
    update((l ushr 40).toByte())
    update((l ushr 32).toByte())
    update((l ushr 24).toByte())
    update((l ushr 16).toByte())
    update((l ushr 8).toByte())
    update(l.toByte())
}

fun MessageDigest.updateValue(s: String) {
    updateValue(s.toByteArray())
}

fun MessageDigest.updateValue(b: BigDecimal) {
    updateValue(b.scale())
    updateValue(b.unscaledValue().toByteArray())
}

fun MessageDigest.updateValue(b: BigInteger) {
    val bytes = b.toByteArray()
    updateValue(0) // A "scale" of zero
    updateValue(bytes)
}

fun MessageDigest.updateValue(bytes: ByteArray) {
    updateValue(bytes.size)
    update(bytes)
}