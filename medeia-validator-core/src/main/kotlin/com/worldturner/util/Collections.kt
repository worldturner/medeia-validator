package com.worldturner.util

inline fun <K, VI, VO> Map<K, VI>.mapToMapTo(
    destination: MutableMap<K, VO>,
    transform: (Map.Entry<K, VI>) -> Pair<K, VO>
): MutableMap<K, VO> {
    for (item in this) {
        val transformed = transform(item)
        destination[transformed.first] = transformed.second
    }
    return destination
}

inline fun <T> MutableCollection<T>.iterate(action: (element: T, iterator: MutableIterator<T>) -> Unit) {
    val iterator = this.iterator()
    while (iterator.hasNext()) {
        val element = iterator.next()
        action(element, iterator)
    }
}

fun <T> List<T>.repeat(times: Int): List<T> =
    when (times) {
        0 -> emptyList<T>()
        1 -> this
        else -> when (size) {
            0 -> emptyList<T>()
            else -> {
                val result = ArrayList<T>(size * times)
                for (i in 1..times) {
                    result.addAll(this)
                }
                result
            }
        }
    }
