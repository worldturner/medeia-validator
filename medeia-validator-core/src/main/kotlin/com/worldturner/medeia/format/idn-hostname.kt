package com.worldturner.medeia.format

import com.worldturner.medeia.format.i18n.Punycode
import com.worldturner.util.toCodePoints

fun String.isIdnHostname() =
    this.idnToLdhHostname().isHostname()

fun String.idnToLdhHostname(): String =
    this.split('.').map { it.idnToLdhLabel() }.joinToString(".")

fun String.idnToLdhLabel(): String =
    if (this.toCodePoints().count { Punycode.parameters.isBasicCodePoint(it) } == this.length) {
        // Label is not internationalized
        this
    } else {
        "xn--${Punycode.encode(this)}"
    }
