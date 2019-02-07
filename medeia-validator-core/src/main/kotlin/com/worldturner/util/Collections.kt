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