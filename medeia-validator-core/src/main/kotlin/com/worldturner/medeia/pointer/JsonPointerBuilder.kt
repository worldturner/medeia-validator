package com.worldturner.medeia.pointer

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenType

class JsonPointerBuilder : JsonTokenDataConsumer {
    val stack: ArrayList<JsonPointerEntry> = ArrayList()

    override fun consume(token: JsonTokenData) {
        stack.lastOrNull()?.incrementIfNecessary()
        when (token.type) {
            JsonTokenType.START_OBJECT -> stack.add(JsonPointerObject())
            JsonTokenType.END_OBJECT -> stack.removeAt(stack.lastIndex)
            JsonTokenType.START_ARRAY -> stack.add(JsonPointerArray())
            JsonTokenType.END_ARRAY -> stack.removeAt(stack.lastIndex)
            JsonTokenType.FIELD_NAME -> stack.last().nextProperty(token.text!!)
            else -> Unit
        }
        if (token.type != JsonTokenType.FIELD_NAME) {
            incrementOnNextRun()
        }
    }

    private fun incrementOnNextRun() = stack.lastOrNull()?.incrementOnNextRun()

    private fun jsonPointerLength(): Int {
        var length = 0
        val size = stack.size
        for (i in 0 until size) {
            length += stack[i].stringLength()
        }
        return length
    }

    fun toJsonPointer(): JsonPointer {
        val b = StringBuilder(jsonPointerLength())
        val size = stack.size
        for (i in 0 until size) {
            stack[i].toString(b)
        }
        return JsonPointer(b.toString(), bypassValidation = true)
    }
}

interface JsonPointerEntry {
    fun nextProperty(propertyName: String)

    fun incrementIfNecessary()
    fun incrementOnNextRun()

    fun stringLength(): Int
    fun toString(b: StringBuilder)
}

class JsonPointerObject : JsonPointerEntry {
    var propertyName: String? = null

    override fun incrementIfNecessary() = Unit
    override fun incrementOnNextRun() = Unit
    override fun nextProperty(propertyName: String) {
        this.propertyName = propertyName
    }

    override fun toString(b: StringBuilder) {
        val propertyName = this.propertyName
        if (propertyName != null) {
            b.append('/')
            for (ch in propertyName) {
                if (ch == '~') {
                    b.append("~0")
                } else if (ch == '/') {
                    b.append("~1")
                } else {
                    b.append(ch)
                }
            }
        }
    }

    override fun stringLength(): Int {
        val propertyName = this.propertyName
        if (propertyName != null) {
            var length = 1
            for (ch in propertyName) {
                if (ch == '~' || ch == '/') {
                    length += 2
                } else {
                    length++
                }
            }
            return length
        } else {
            return 0
        }
    }
}

class JsonPointerArray : JsonPointerEntry {
    var index: Int = -1
    var incrementOnNextRun = false

    override fun nextProperty(propertyName: String) = throw UnsupportedOperationException()

    override fun incrementIfNecessary() {
        if (incrementOnNextRun) {
            index++
            incrementOnNextRun = false
        }
    }

    override fun incrementOnNextRun() {
        incrementOnNextRun = true
    }

    override fun toString(b: StringBuilder) {
        if (index != -1) {
            b.append('/')
            b.append(index)
        }
    }

    override fun stringLength(): Int {
        if (index == 0) {
            return 2
        } else if (index != -1) {
            var i = index
            var length = 1
            while (i > 0) {
                length++
                i /= 10
            }
            return length
        } else {
            return 0
        }
    }
}
