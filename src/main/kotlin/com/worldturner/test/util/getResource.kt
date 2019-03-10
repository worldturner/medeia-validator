package com.worldturner.test.util

import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL

inline fun <reified T> T.getResourceAsStream(resource: String): InputStream {
    val stream = T::class.java.getResourceAsStream(resource)
    return stream ?: throw FileNotFoundException("Resource $resource not found")
}

inline fun <reified T> T.getResourceAsUrl(resource: String): URL {
    val stream = T::class.java.getResource(resource)
    return stream ?: throw FileNotFoundException("Resource $resource not found")
}