package com.worldturner.medeia.parser.jackson

import com.fasterxml.jackson.core.Base64Variant
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonStreamContext
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.core.SerializableString
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.Version
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.FIELD_NAME
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NUMBER
import com.worldturner.medeia.parser.TOKEN_EMPTY_STRING
import com.worldturner.medeia.parser.TOKEN_END_ARRAY
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_FALSE
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_START_ARRAY
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.TOKEN_TRUE
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.pointer.JsonPointerBuilder
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayDeque

class JacksonTokenDataJsonGenerator(
    val consumer: JsonTokenDataAndLocationConsumer,
    val delegate: JsonGenerator
) : JsonGenerator() {
    var level: Int = 0
        private set

    private val jsonPointerBuilder = JsonPointerBuilder()

    private val dynamicTokenLocation = DynamicJsonTokenLocation()

    private val propertyNamesStack = ArrayDeque<MutableSet<String>>()

    inner class DynamicJsonTokenLocation : JsonTokenLocation {
        override val level: Int
            get() = this@JacksonTokenDataJsonGenerator.level
        override val pointer: JsonPointer
            get() = jsonPointerBuilder.toJsonPointer()
        override val propertyNames: Set<String>
            get() = propertyNamesStack.peek() ?: emptySet()

        override fun toString(): String = "at $pointer"
    }

    override fun getOutputContext(): JsonStreamContext {
        return delegate.outputContext
    }

    @Suppress("DEPRECATION")
    override fun setFeatureMask(values: Int): JsonGenerator {
        delegate.featureMask = values
        return this
    }

    override fun setCodec(oc: ObjectCodec?): JsonGenerator {
        delegate.codec = oc
        return this
    }

    override fun isEnabled(f: Feature): Boolean = delegate.isEnabled(f)

    override fun disable(f: Feature): JsonGenerator {
        delegate.disable(f)
        return this
    }

    override fun close() {
        delegate.close()
    }

    override fun isClosed(): Boolean = delegate.isClosed

    override fun writeStartArray() {
        consumer.consume(TOKEN_START_ARRAY, dynamicTokenLocation)
        delegate.writeStartArray()
        level++
    }

    override fun writeEndArray() {
        level--
        consumer.consume(TOKEN_END_ARRAY, dynamicTokenLocation)
        delegate.writeEndArray()
    }

    override fun writeStartObject() {
        consumer.consume(TOKEN_START_OBJECT, dynamicTokenLocation)
        propertyNamesStack.addFirst(HashSet())
        delegate.writeStartObject()
        level++
    }

    override fun writeEndObject() {
        level--
        consumer.consume(TOKEN_END_OBJECT, dynamicTokenLocation)
        delegate.writeEndObject()
        propertyNamesStack.removeFirst()
    }

    override fun writeNull() {
        consumer.consume(TOKEN_NULL, dynamicTokenLocation)
        delegate.writeNull()
    }

    override fun writeNumber(v: Int) {
        consumer.consume(JsonTokenData.createNumber(v.toLong()), dynamicTokenLocation)
        delegate.writeNumber(v)
    }

    override fun writeNumber(v: Long) {
        consumer.consume(JsonTokenData.createNumber(v), dynamicTokenLocation)
        delegate.writeNumber(v)
    }

    override fun writeNumber(v: BigInteger?) {
        val tokenData = JsonTokenData(VALUE_NUMBER, integer = v)
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeNumber(v)
    }

    override fun writeNumber(v: Double) {
        val tokenData = JsonTokenData(VALUE_NUMBER, decimal = BigDecimal.valueOf(v))
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeNumber(v)
    }

    override fun writeNumber(v: Float) {
        val tokenData = JsonTokenData(
            VALUE_NUMBER,
            decimal = BigDecimal.valueOf(v.toDouble())
        )
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeNumber(v)
    }

    override fun writeNumber(v: BigDecimal) {
        val tokenData = JsonTokenData(VALUE_NUMBER, decimal = v)
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeNumber(v)
    }

    override fun writeNumber(encodedValue: String) {
        val tokenData = JsonTokenData(VALUE_NUMBER, decimal = BigDecimal(encodedValue))
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeNumber(encodedValue)
    }

    override fun writeString(text: String) {
        val tokenData = JsonTokenData.createText(text)
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeString(text)
    }

    override fun writeString(text: CharArray, offset: Int, len: Int) {
        val tokenData =
            if (len == 0)
                TOKEN_EMPTY_STRING
            else
                JsonTokenData.createText(String(text, offset, len))
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeString(text, offset, len)
    }

    override fun writeString(text: SerializableString) {
        val tokenData = JsonTokenData.createText(text.value)
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeString(text)
    }

    override fun writeUTF8String(text: ByteArray, offset: Int, length: Int) =
        throw UnsupportedOperationException()

    override fun writeBoolean(state: Boolean) {
        val tokenData = if (state) TOKEN_TRUE else TOKEN_FALSE
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeBoolean(state)
    }

    override fun writeFieldName(name: String) {
        val tokenData = JsonTokenData(FIELD_NAME, text = name)
        consumer.consume(tokenData, dynamicTokenLocation)
        propertyNamesStack.peek() += name
        delegate.writeFieldName(name)
    }

    override fun writeFieldName(name: SerializableString) {
        val text = name.value
        val tokenData = JsonTokenData(FIELD_NAME, text = text)
        consumer.consume(tokenData, dynamicTokenLocation)
        propertyNamesStack.peek() += text
        delegate.writeFieldName(name)
    }

    override fun writeBinary(bv: Base64Variant, data: ByteArray, offset: Int, len: Int) {
        val tokenData = JsonTokenData.createText(bv.encode(data.copyOfRange(offset, offset + len)))
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeBinary(bv, data, offset, len)
    }

    override fun writeBinary(bv: Base64Variant, data: InputStream, dataLength: Int): Int {
        val bytes = data.readBytesLength(dataLength)
        val tokenData = JsonTokenData.createText(bv.encode(bytes))
        consumer.consume(tokenData, dynamicTokenLocation)
        delegate.writeBinary(bv, bytes, 0, bytes.size)
        return dataLength
    }

    override fun writeRawUTF8String(text: ByteArray?, offset: Int, length: Int) = throw UnsupportedOperationException()

    override fun writeRawValue(text: String) = throw UnsupportedOperationException()

    override fun writeRawValue(text: String, offset: Int, len: Int) = throw UnsupportedOperationException()

    override fun writeRawValue(text: CharArray, offset: Int, len: Int) = throw UnsupportedOperationException()

    override fun writeRaw(text: String) = throw UnsupportedOperationException()

    override fun writeRaw(text: String, offset: Int, len: Int) = throw UnsupportedOperationException()

    override fun writeRaw(text: CharArray, offset: Int, len: Int) = throw UnsupportedOperationException()

    override fun writeRaw(c: Char) = throw UnsupportedOperationException()

    override fun useDefaultPrettyPrinter(): JsonGenerator {
        delegate.useDefaultPrettyPrinter()
        return this
    }

    override fun flush() = delegate.flush()

    override fun getFeatureMask(): Int = delegate.featureMask

    override fun enable(f: Feature?): JsonGenerator {
        delegate.enable(f)
        return this
    }

    override fun getCodec(): ObjectCodec = delegate.codec

    override fun writeObject(pojo: Any?) = throw UnsupportedOperationException()

    override fun writeTree(rootNode: TreeNode) = throw UnsupportedOperationException()

    override fun version(): Version = delegate.version()
}

fun InputStream.readBytesLength(len: Int): ByteArray {
    var remaining = len
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(16384)
    var bytes = read(buffer, 0, Math.min(remaining, buffer.size))
    while (bytes >= 0) {
        output.write(buffer, 0, bytes)
        remaining -= bytes
        bytes = read(buffer)
    }
    return output.toByteArray()
}