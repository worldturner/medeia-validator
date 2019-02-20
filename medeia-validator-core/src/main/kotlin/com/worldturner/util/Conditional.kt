package com.worldturner.util

/** Returns the result of action if this Boolean equals true, else returns null. */
inline fun <T> Boolean.orNull(action: () -> T): T? =
    if (this) action() else null

/** Executes ation if this Boolean equals true. */
inline fun Boolean.ifTrue(action: () -> Unit) {
    if (this) action()
}