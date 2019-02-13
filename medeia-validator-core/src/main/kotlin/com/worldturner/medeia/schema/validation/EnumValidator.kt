package com.worldturner.medeia.schema.validation

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.parser.ArrayNodeData
import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.ObjectNodeData
import com.worldturner.medeia.parser.TokenNodeData
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import com.worldturner.util.iterate
import java.util.ArrayDeque
import java.util.Deque

class EnumValidator(
    val enum: Set<NodeData>
) : SchemaValidator {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance =
        EnumValidatorInstance(startLevel, enum)

    companion object {
        fun create(enum: Set<NodeData>? = null): SchemaValidator? =
            enum?.let {
                if (enum.all { it is TokenNodeData })
                    @Suppress("UNCHECKED_CAST")
                    TokenOnlyEnumValidator(enum as Set<TokenNodeData>)
                else
                    EnumValidator(enum)
            }
    }
}

internal class TokenOnlyEnumValidator(
    val enum: Set<TokenNodeData>
) : SchemaValidator, SchemaValidatorInstance {
    override fun createInstance(startLevel: Int): SchemaValidatorInstance = this

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (!token.type.nonStructureToken) {
            return fail(location, "None of the non-structured enum values matched data $token")
        }
        return if (enum.any { it.token == token })
            OkValidationResult
        else
            fail(location, "None of the non-structured enum values matched data $token")
    }
}

class EnumValueState(enumValue: NodeData) {
    internal val constStack: Deque<NodeData> = ArrayDeque()
    internal var currentConst: NodeData? = enumValue
}

class EnumValidatorInstance(
    val startLevel: Int,
    val enum: Set<NodeData>
) : SchemaValidatorInstance {
    private val verificationStack: Deque<ConstVerifier> = ArrayDeque()
    private var currentProperty: String? = null
    private val enumValueStates = enum.mapTo(mutableListOf()) { EnumValueState(it) }

    override fun validate(token: JsonTokenData, location: JsonTokenLocation): ValidationResult? {
        if (token.type == JsonTokenType.FIELD_NAME) {
            currentProperty = token.text!!
            return null
        }
        if (token.type.firstToken) {
            val top = if (verificationStack.isEmpty()) null else verificationStack.peek()
            when (top) {
                is ObjectVerifier -> {
                    top.propertyNames.add(currentProperty!!)
                    enumValueStates.iterate { enumValueState, iterator ->
                        val currentConst = enumValueState.currentConst
                        if (currentConst is ObjectNodeData) {
                            enumValueState.constStack.push(currentConst)
                            currentConst.nodes[currentProperty!!]?.let { enumValueState.currentConst = it }
                                ?: iterator.remove()
                        } else {
                            iterator.remove()
                        }
                    }
                }
                is ArrayVerifier -> {
                    enumValueStates.iterate { enumValueState, iterator ->
                        val currentConst = enumValueState.currentConst
                        if (currentConst is ArrayNodeData) {
                            if (currentConst.nodes.size <= top.itemCount) {
                                iterator.remove()
                            } else {
                                enumValueState.constStack.push(currentConst)
                                enumValueState.currentConst = currentConst.nodes[top.itemCount]
                            }
                        } else {
                            iterator.remove()
                        }
                    }
                    top.itemCount++
                }
            }
            val verifier =
                when (token.type) {
                    JsonTokenType.START_OBJECT -> ObjectVerifier()
                    JsonTokenType.START_ARRAY -> ArrayVerifier()
                    else -> SingleVerifier(token)
                }
            verificationStack.push(verifier)
        }
        if (token.type.lastToken) {
            val top = verificationStack.pop()!!
            enumValueStates.iterate { enumValueState, iterator ->
                val result = top.verify(enumValueState.currentConst!!, location)
                if (!result.valid)
                    iterator.remove()
                else
                    enumValueState.currentConst =
                        if (enumValueState.constStack.isEmpty()) null else enumValueState.constStack.pop()
            }
            if (location.level == startLevel) {
                return if (enumValueStates.isNotEmpty())
                    OkValidationResult
                else
                    fail(location, "None of the enum values matched")
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
        rule = "enum",
        message = message
    )
