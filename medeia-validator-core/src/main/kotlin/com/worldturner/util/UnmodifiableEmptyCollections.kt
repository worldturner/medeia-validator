package com.worldturner.util

import java.util.Collections
import java.util.Deque
import java.util.NoSuchElementException

@Suppress("UNCHECKED_CAST")
fun <K, V> unmodifiableEmptyMutableMap(): UnmodifiableEmptyMutableMap<K, V> =
    UnmodifiableEmptyMutableMap.INSTANCE as UnmodifiableEmptyMutableMap<K, V>

class UnmodifiableEmptyMutableMap<K, V> : MutableMap<K, V>
by Collections.unmodifiableMap(Collections.emptyMap()) {

    override fun clear() = Unit

    override fun remove(key: K): V? = null

    override fun remove(key: K, value: V): Boolean = false

    override fun containsKey(key: K): Boolean = false

    companion object {
        val INSTANCE = UnmodifiableEmptyMutableMap<Nothing, Nothing>()
    }
}

@Suppress("UNCHECKED_CAST")
fun <E> emptyMutableIterator(): EmptyMutableIterator<E> =
    EmptyMutableIterator.INSTANCE as EmptyMutableIterator<E>

class EmptyMutableIterator<E> : MutableIterator<E> {
    override fun hasNext(): Boolean = false
    override fun next(): E = throw NoSuchElementException()
    override fun remove() = Unit

    companion object {
        val INSTANCE = EmptyMutableIterator<Nothing>()
    }
}

@Suppress("UNCHECKED_CAST")
fun <E> unmodifiableEmptyDeque(): UnmodifiableEmptyDeque<E> =
    UnmodifiableEmptyDeque.INSTANCE as UnmodifiableEmptyDeque<E>

class UnmodifiableEmptyDeque<E> : Deque<E> {
    override val size: Int
        get() = 0

    override fun removeFirstOccurrence(o: Any?): Boolean = false
    override fun pop(): E = throw NoSuchElementException()
    override fun poll(): E? = null
    override fun pollFirst(): E? = null
    override fun pollLast(): E? = null
    override fun retainAll(elements: Collection<E>): Boolean = elements.isEmpty()
    override fun removeLastOccurrence(o: Any?): Boolean = false
    override fun isEmpty(): Boolean = true
    override fun remove(): E = throw NoSuchElementException()
    override fun remove(element: E): Boolean = false
    override fun removeLast(): E = throw NoSuchElementException()
    override fun containsAll(elements: Collection<E>): Boolean = elements.isEmpty()
    override fun offer(e: E): Boolean = false
    override fun iterator(): MutableIterator<E> = emptyMutableIterator()
    override fun offerLast(e: E): Boolean = false
    override fun offerFirst(e: E): Boolean = false
    override fun removeFirst(): E = throw NoSuchElementException()
    override fun getFirst(): E = throw NoSuchElementException()
    override fun removeAll(elements: Collection<E>): Boolean = false

    override fun getLast(): E = throw NoSuchElementException()

    override fun add(element: E): Boolean = throw UnsupportedOperationException()

    override fun addFirst(e: E) = throw UnsupportedOperationException()

    override fun addLast(e: E) = throw UnsupportedOperationException()
    override fun contains(element: E): Boolean = false
    override fun descendingIterator(): MutableIterator<E> = emptyMutableIterator()

    override fun element(): E = throw NoSuchElementException()

    override fun peekLast(): E? = null

    override fun peekFirst(): E? = null

    override fun peek(): E? = null

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach { add(it) }
        return false
    }

    override fun clear() = Unit

    override fun push(e: E) = throw UnsupportedOperationException()

    companion object {
        val INSTANCE = UnmodifiableEmptyDeque<Nothing>()
    }
}
