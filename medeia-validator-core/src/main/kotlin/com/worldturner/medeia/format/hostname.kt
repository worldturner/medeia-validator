package com.worldturner.medeia.format

// RFC1034 3.1 & 3.5
fun String.isHostname(): Boolean {
    if (this.length > 255) return false
    val labels = this.split('.')
    if (!labels.all { it.isHostnameLabel() }) return false
    // Labels must have at least one character except for the last label
    // but if there is only one label it has to be non-empty
    val notEmptyUpTo = if (labels.size == 1) 1 else labels.size - 1
    for (i in 0 until notEmptyUpTo)
        if (labels[i].isEmpty()) return false
    return true
}

// RFC1034 3.1 & 3.5
fun String.isHostnameLabel(): Boolean {
    val length = this.length
    if (length > 63) return false
    for (i in 0 until length) {
        if (i == 0) {
            when (this[i]) {
                in 'a'..'z', in 'A'..'Z' -> {
                }
                else -> return false
            }
        } else if (i == length - 1) {
            when (this[i]) {
                in '0'..'9', in 'a'..'z', in 'A'..'Z' -> {
                }
                else -> return false
            }
        } else {
            when (this[i]) {
                in '0'..'9', in 'a'..'z', in 'A'..'Z', '-' -> {
                }
                else -> return false
            }
        }
    }
    return true
}