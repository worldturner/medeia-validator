package com.worldturner.medeia.format.i18n

object Punycode {
    val parameters = BootstringParameters(
        base = 36, tmin = 1, tmax = 26, skew = 38, damp = 700,
        initialBias = 72, initialN = 0x80, delimiter = '-'.toInt(),
        basicCodePoints = "abcdefghijklmnopqrstuvwxyz0123456789"
    )

    fun encode(s: String) = Bootstring.encode(
        s,
        parameters
    )
}