package com.worldturner.medeia.format

import com.worldturner.medeia.format.i18n.Punycode

fun String.isIdnHostname() =
    this.idnToLdhHostname().isHostname()

fun String.idnToLdhHostname(): String =
    this.split('.').map { it.idnToLdhLabel() }.joinToString(".")

fun String.idnToLdhLabel(): String =
    if (this.count { Punycode.parameters.isBasicCodePoint(it) } == this.length) {
        // Label is not internationalized
        this
    } else {
        "xn--${Punycode.encode(this)}"
    }
