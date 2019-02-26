package com.worldturner.util

fun StringBuilder.appendJsonString(s: CharSequence): StringBuilder {
    this.append('"')
    s.forEach {
        when (it) {
            '\n' -> this.append("\\n")
            '\r' -> this.append("\\r")
            '"' -> this.append("\\\"")
            '\\' -> this.append("\\\\")
            '\b' -> this.append("\\b")
            '\u000c' -> this.append("\\f")
            '\t' -> this.append("\\t")
            else -> if (it < ' ')
                this.append("\\u00${String.format("%02X", it)}")
            else
                this.append(it)
        }
    }
    this.append('"')
    return this
}
