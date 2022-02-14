package com.worldturner.util

import java.net.URI

fun URI.hasAnyOtherThanFragment() =
    !rawAuthority.isNullOrEmpty() || !rawPath.isNullOrEmpty() || !rawQuery.isNullOrEmpty() ||
        !rawSchemeSpecificPart.isNullOrEmpty() || !rawUserInfo.isNullOrEmpty()

fun URI.hasFragment() = this.fragment != null
fun URI.withoutFragment(): URI = URI(this.toString().replaceFromLast('#', ""))

fun URI.withEmptyFragment(): URI = this.toString().let {
    URI(it.replaceAfterLast('#', "", "$it#"))
}

fun URI.replaceFragment(fragment: String, encoded: Boolean = false) = try {
    URI.create(
        if (hasFragment())
            this.toString().replaceAfter(
                '#',
                if (encoded) encodeConservatively(fragment) else fragment
            )
        else
            "$this#${if (encoded) encodeConservatively(fragment) else fragment}"
    )!!
} catch (e: IllegalArgumentException) {
    throw e
}

// cannot contain a #, a raw %, ^, [, ], {, }, \, ", < and >
fun encodeConservatively(s: String): String =
    s.replace(Regex("[\\\\|#<>\\^%\\[\\]{}\" ]")) { r ->
        "%" + r.value[0].toInt().toString(16)
    }

// TODO: fix broken urn handling when using fragments as relative URI's
fun URI.resolveSafe(relative: URI) = run {
    val relativeStr = relative.toString()
    if (relativeStr.startsWith("#"))
        this.replaceFragment(relativeStr.substring(1))
    else
        this.resolve(relative)!!
}

val EMPTY_URI = URI.create("")!!