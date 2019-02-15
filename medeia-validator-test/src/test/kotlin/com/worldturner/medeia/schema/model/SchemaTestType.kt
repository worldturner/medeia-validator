package com.worldturner.medeia.schema.model

import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.parser.type.AnyOfType
import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.parser.type.BooleanType
import com.worldturner.medeia.parser.type.JsonAsStringType
import com.worldturner.medeia.parser.type.ObjectType
import com.worldturner.medeia.parser.type.PropertyType
import com.worldturner.medeia.parser.type.ReferenceType
import com.worldturner.medeia.parser.type.SimpleTreeType
import com.worldturner.medeia.parser.type.TextType

object SchemaTestType : ObjectType(
    SchemaTest::class,
    propertyTypes = listOf(
        PropertyType("description", TextType),
        PropertyType("schema", JsonAsStringType),
        PropertyType("tests", ArrayType(SchemaTestCaseType), readOnly = true),
        PropertyType("path", TextType)
    )
)

object SchemaTestCaseType : ObjectType(
    SchemaTestCase::class,
    propertyTypes = listOf(
        PropertyType("description", TextType),
        PropertyType("data", SimpleTreeType),
        PropertyType("valid", BooleanType)
    )
)

object TestResultType : ObjectType(
    TestResult::class,
    propertyTypes = listOf(
        PropertyType("test", SchemaTestType),
        PropertyType("case", SchemaTestCaseType),
        PropertyType("outcome", ValidationResultType),
        PropertyType("testSucceeded", BooleanType)
    )
)

object ValidationResultType : AnyOfType(
    classMap = mapOf(
        OkValidationResult::class to ObjectType(
            OkValidationResult::class,
            propertyTypes = listOf(
                PropertyType("valid", BooleanType)
            )
        ),
        FailedValidationResult::class to ObjectType(
            FailedValidationResult::class,
            propertyTypes = listOf(
                PropertyType("rule", TextType),
                PropertyType("property", TextType),
                PropertyType("message", TextType),
                PropertyType("location", TextType),
                PropertyType("details", ArrayType(ValidationResultTypeReference)),
                PropertyType("valid", BooleanType)
            )
        )
    )
)

object ValidationResultTypeReference : ReferenceType({ ValidationResultType })
