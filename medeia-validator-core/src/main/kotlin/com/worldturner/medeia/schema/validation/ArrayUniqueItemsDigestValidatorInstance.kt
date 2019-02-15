package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataAndLocationBuilder
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.JsonTokenType.END_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.schema.validation.NodeHasher.Companion.TYPE_ARRAY_END
import com.worldturner.medeia.schema.validation.NodeHasher.Companion.TYPE_ARRAY_START
import com.worldturner.medeia.schema.validation.NodeHasher.Companion.TYPE_OBJECT_END
import com.worldturner.medeia.schema.validation.NodeHasher.Companion.TYPE_OBJECT_START
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import com.worldturner.util.updateValue
import java.net.URI
import java.security.MessageDigest
import java.util.ArrayDeque
import java.util.Deque
import java.util.TreeMap

class ArrayUniqueItemsDigestValidator(val digestAlgorithm: String) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        ArrayUniqueItemsDigestValidatorInstance(startLevel) {
            MessageDigest.getInstance(digestAlgorithm)!!
        }

    override fun recordUnknownRefs(unknownRefs: MutableCollection<URI>) = Unit
}

class ArrayUniqueItemsDigestValidatorInstance(
    val startLevel: Int,
    factory: MessageDigestFactory
) : SchemaValidatorInstance {

    private val uniqueItemHashes: MutableSet<HashResult> = mutableSetOf()
    private val digestBuilder: DigestBuilder = DigestBuilder(startLevel + 1, factory)

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (location.level == startLevel && token.type != START_ARRAY) {
            return OkValidationResult
        }
        if (location.level > startLevel) {
            digestBuilder.consume(token, location)
            digestBuilder.takeResult()?.let { hashResult ->
                if (hashResult in uniqueItemHashes) {
                    return FailedValidationResult(
                        location = location,
                        rule = "uniqueItems",
                        message = "Duplicate item based on hash"
                    )
                } else {
                    uniqueItemHashes.add(hashResult)
                }
            }
        }
        if (token.type == END_ARRAY && location.level == startLevel)
            return OkValidationResult
        else
            return null
    }
}

/*
 * Note: an important principle of hashing is that the bytes sent to the hash should
 * be "deserializable" if need be, to ensure that we don't have two equal byte streams for
 * different source data. There is a always a chance of hash collisions but we should
 * ensure that there are no data collisions *before* hashing.
 */

interface NodeHasher {
    fun digest(digester: MessageDigest)

    companion object {
        const val TYPE_TOKEN = 1.toByte()
        const val TYPE_ARRAY_START = 2.toByte()
        const val TYPE_ARRAY_END = 3.toByte()
        const val TYPE_OBJECT_START = 4.toByte()
        const val TYPE_OBJECT_END = 5.toByte()
    }
}

interface NodeHasherParent : NodeHasher {
    val parentDigester: MessageDigest?
    fun add(hasher: NodeHasher)
}

typealias MessageDigestFactory = () -> MessageDigest

class ObjectNodeHasher(val factory: MessageDigestFactory) : NodeHasherParent {
    // Hashes keyes by sorted property names
    val objectHashes = TreeMap<String, ByteArray>()
    var currentProperty: String? = null
    override var parentDigester: MessageDigest? = null
        get() {
            if (field == null) {
                field = factory()
            }
            return field
        }

    override fun add(hasher: NodeHasher) {
        val digester = parentDigester!!
        hasher.digest(digester)
        val hash = digester.digest()
        objectHashes[currentProperty!!] = hash
        parentDigester = null
    }

    /*
     * Unfortunately, hashes for an object can only be created when the object is complete, since the
     * properties need to be sorted first (i.e. 2 objects with the same property names and values, but with the
     * properties in a different order, are still considered the same)
     */
    override fun digest(digester: MessageDigest) {
        digester.update(TYPE_OBJECT_START)
        objectHashes.forEach {
            digester.updateValue(it.key)
            digester.update(it.value)
        }
        digester.update(TYPE_OBJECT_END)
    }
}

class ArrayNodeHasher(override val parentDigester: MessageDigest) : NodeHasherParent {
    init {
        parentDigester.update(TYPE_ARRAY_START)
    }

    override fun add(hasher: NodeHasher) {
        hasher.digest(parentDigester)
    }

    override fun digest(digester: MessageDigest) {
        if (digester != parentDigester)
            throw IllegalStateException("Digesters differ")
        digester.update(TYPE_ARRAY_END)
    }
}

/*
 * Code is optimized such that single JsonTokenData "trees" are not hashed.
 * A JsonTokenData is also its own NodeHasher and its own HashResult if it is the single result of the builder.
 */
class DigestBuilder(
    val startLevel: Int,
    val factory: MessageDigestFactory
) : JsonTokenDataAndLocationBuilder {
    private val stack: Deque<NodeHasher> = ArrayDeque()
    private var result: HashResult? = null

    override fun consume(token: JsonTokenData, location: JsonTokenLocation) {
        if (token.type == JsonTokenType.FIELD_NAME) {
            processFieldName(token)
            return
        } else {
            if (token.type.firstToken) {
                val nodeHasher =
                    when (token.type) {
                        JsonTokenType.START_OBJECT -> ObjectNodeHasher(factory)
                        JsonTokenType.START_ARRAY -> {
                            val parentDigester = findParentDigester()
                            ArrayNodeHasher(parentDigester)
                        }
                        else -> token
                    }
                stack.push(nodeHasher)
            }
            if (token.type.lastToken) {
                val nodeHasher = stack.pop()!!
                val top: NodeHasher? = stack.peek()
                if (top is NodeHasherParent) {
                    top.add(nodeHasher)
                } else if (location.level == startLevel) {
                    lastStep(nodeHasher)
                } else {
                    throw IllegalStateException("Child node seen where this is not allowed")
                }
            }
        }
    }

    private fun lastStep(nodeHasher: NodeHasher) {
        when (nodeHasher) {
            is JsonTokenData -> result = nodeHasher
            is ArrayNodeHasher -> {
                val digester = nodeHasher.parentDigester
                nodeHasher.digest(digester)
                result = DigestHashResult(digester.digest())
            }
            is ObjectNodeHasher -> {
                val digester = factory()
                nodeHasher.digest(digester)
                result = DigestHashResult(digester.digest())
            }
        }
    }

    private fun findParentDigester(): MessageDigest {
        val top: NodeHasher? = stack.peek()
        val parentDigester =
            if (top is NodeHasherParent) top.parentDigester!! else factory()
        return parentDigester
    }

    private fun processFieldName(token: JsonTokenData) {
        if (stack.isNotEmpty()) {
            val top = stack.peek()
            if (top is ObjectNodeHasher) {
                top.currentProperty = token.text!!
                return
            }
        }
        throw IllegalStateException("Received field name outside of Object context")
    }

    override fun takeResult(): HashResult? {
        val r = result
        result = null
        return r
    }
}

/**
 * Polymorphic parent interface to optimize for the case of a single non-structured token. In that case,
 * the token is not hashed (JsonTokenData implements HashResult). If compared against the other implementation
 * of this interface, DigestHashResult, it always returns false.
 */
interface HashResult

/**
 * A simple wrapper around a byte array.
 */
data class DigestHashResult(val digestBytes: ByteArray) : HashResult {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DigestHashResult
        if (!digestBytes.contentEquals(other.digestBytes)) return false
        return true
    }

    override fun hashCode(): Int = digestBytes.contentHashCode()
}
