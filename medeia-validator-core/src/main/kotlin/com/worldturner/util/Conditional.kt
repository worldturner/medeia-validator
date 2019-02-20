package com.worldturner.util

inline fun <T> Boolean.orNull(action: () -> T): T? =
    if (this) action() else null