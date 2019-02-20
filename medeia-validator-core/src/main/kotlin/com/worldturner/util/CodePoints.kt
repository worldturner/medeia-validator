package com.worldturner.util

fun String.toCodePoints(): Collection<Int> {
    return object : Collection<Int> {
        override fun iterator(): Iterator<Int> {
            return object : Iterator<Int> {
                var nextIndex = 0
                override fun hasNext(): Boolean {
                    return nextIndex < length
                }

                override fun next(): Int {
                    val result = codePointAt(nextIndex)
                    nextIndex += Character.charCount(result)
                    return result
                }
            }
        }

        override fun isEmpty(): Boolean = length == 0

        override val size: Int
            get() = codePointCount(0, length)

        override fun contains(element: Int): Boolean {
            val length = length
            var offset = 0
            while (offset < length) {
                val codePoint = codePointAt(offset)
                if (element == element) return true
                offset += Character.charCount(codePoint)
            }
            return false
        }

        override fun containsAll(elements: Collection<Int>): Boolean =
            elements.all { contains(it) }
    }
}
