package com.worldturner.medeia.parser

enum class JsonTokenType(
    val firstStructureToken: Boolean = false,
    val lastStructureToken: Boolean = false,
    val booleanToken: Boolean = false,
    val nonStructureToken: Boolean = false,
    val syntheticType: Boolean = false,
    val firstToken: Boolean = nonStructureToken || firstStructureToken,
    val lastToken: Boolean = nonStructureToken || lastStructureToken
) {
    NONE(syntheticType = true),
    END_OF_STREAM(syntheticType = true),
    START_OBJECT(firstStructureToken = true),
    END_OBJECT(lastStructureToken = true),
    START_ARRAY(firstStructureToken = true),
    END_ARRAY(lastStructureToken = true),
    FIELD_NAME,
    VALUE_NUMBER(nonStructureToken = true),
    VALUE_TEXT(nonStructureToken = true),
    VALUE_BOOLEAN_TRUE(nonStructureToken = true, booleanToken = true),
    VALUE_BOOLEAN_FALSE(nonStructureToken = true, booleanToken = true),
    VALUE_NULL(nonStructureToken = true);
}
