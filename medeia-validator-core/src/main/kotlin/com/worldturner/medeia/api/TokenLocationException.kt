package com.worldturner.medeia.api

open class TokenLocationException(
    message: String?,
    val location: String,
    cause: Throwable? = null
) : RuntimeException("$message $location", cause)
