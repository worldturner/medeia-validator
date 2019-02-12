package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.ArrayNodeData
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.FIELD_NAME
import com.worldturner.medeia.parser.JsonTokenType.START_ARRAY
import com.worldturner.medeia.parser.JsonTokenType.START_OBJECT
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.ObjectNodeData
import com.worldturner.medeia.parser.TokenNodeData
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import java.util.ArrayDeque
import java.util.Deque

class ConstValidator(val const: NodeData) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        ConstValidatorInstance(startLevel, const)

    companion object {
        fun create(const: NodeData?): SchemaValidator? =
            const?.let {
                if (const is TokenNodeData)
                    TokenOnlyConstValidator(const)
                else
                    ConstValidator(const)
            }
    }
}

class TokenOnlyConstValidator(val const: TokenNodeData) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (!token.type.nonStructureToken) {
            return fail(location, "Single-value const doesn't match")
        }
        return if (const.token == token)
            OkValidationResult
        else
            fail(location, "Const doesn't match")
    }
}

interface ConstVerifier {
    fun verify(node: NodeData, location: JsonTokenLocation):
        ValidationResult
}

class ObjectVerifier : ConstVerifier {
    val propertyNames = mutableSetOf<String>()

    override fun verify(node: NodeData, location: JsonTokenLocation): ValidationResult =
        if (node !is ObjectNodeData) {
            fail(location, "Type mismatch")
        } else if (node.nodes.keys != propertyNames) {
            fail(
                location,
                "Property names $propertyNames not equal to const " +
                    "property names ${node.nodes.keys}"
            )
        } else {
            OkValidationResult
        }
}

class ArrayVerifier : ConstVerifier {
    var itemCount = 0

    override fun verify(node: NodeData, location: JsonTokenLocation): ValidationResult =
        if (node !is ArrayNodeData) {
            fail(location, "Type mismatch")
        } else if (node.nodes.size != itemCount) {
            fail(
                location,
                "Array length $itemCount not equal to const " +
                    "array length ${node.nodes.size}"
            )
        } else {
            OkValidationResult
        }
}

class SingleVerifier(val token: JsonTokenData) : ConstVerifier {
    override fun verify(node: NodeData, location: JsonTokenLocation): ValidationResult =
        if (node !is TokenNodeData) {
            fail(location, "Type mismatch")
        } else if (node.token != token) {
            fail(
                location,
                "Item $token not equal to const item ${node.token}"
            )
        } else {
            OkValidationResult
        }
}

class ConstValidatorInstance(
    val startLevel: Int,
    val const: NodeData
) : SchemaValidatorInstance {
    private val verificationStack: Deque<ConstVerifier> = ArrayDeque()
    private val constStack: Deque<NodeData> = ArrayDeque()
    private var currentProperty: String? = null
    private var currentConst: NodeData? = const

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type == FIELD_NAME) {
            currentProperty = token.text!!
            return null
        }

        val currentConst = this.currentConst
        if (token.type.firstToken) {
            val top = if (verificationStack.isEmpty()) null else verificationStack.peek()
            when (top) {
                is ObjectVerifier -> {
                    top.propertyNames.add(currentProperty!!)
                    if (currentConst is ObjectNodeData) {
                        this.constStack.push(currentConst)
                        this.currentConst = currentConst.nodes[currentProperty!!]
                            ?: return fail(
                                location,
                                "Const validation failed at level ${verificationStack.size}, " +
                                    "property $currentProperty doesn't exist in const value"
                            )
                    } else {
                        return fail(
                            location,
                            "Const validation failed at level ${verificationStack.size}, " +
                                "const value is not an object at this point"
                        )
                    }
                }
                is ArrayVerifier -> {
                    if (currentConst is ArrayNodeData) {
                        if (currentConst.nodes.size <= top.itemCount)
                            return fail(
                                location,
                                "Const validation failed at level ${verificationStack.size}, " +
                                    "index ${top.itemCount} doesn't exist in array in const value"
                            )
                        this.constStack.push(currentConst)
                        this.currentConst = currentConst.nodes[top.itemCount]
                    } else {
                        return fail(
                            location,
                            "Const validation failed at level ${verificationStack.size}, " +
                                "const value is not an array at this point"
                        )
                    }
                    top.itemCount++
                }
            }
            val verifier =
                when (token.type) {
                    START_OBJECT -> ObjectVerifier()
                    START_ARRAY -> ArrayVerifier()
                    else -> SingleVerifier(token)
                }
            verificationStack.push(verifier)
        }
        if (token.type.lastToken) {
            val top = verificationStack.pop()!!
            val result = top.verify(this.currentConst!!, location)
            if (!result.valid) return result
            this.currentConst = if (constStack.isEmpty()) null else constStack.pop()
            if (location.level == startLevel) {
                return OkValidationResult
            }
        }

        return null
    }
}

private fun fail(
    location: JsonTokenLocation,
    message: String
) =
    FailedValidationResult(
        location = location,
        rule = "const",
        message = message
    )