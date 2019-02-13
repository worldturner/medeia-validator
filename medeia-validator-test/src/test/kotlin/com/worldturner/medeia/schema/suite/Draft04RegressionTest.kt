package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.schema.model.SchemaTestDraft04Type
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import java.net.URI
import java.nio.file.Paths

val DRAFT04_RUNNER = TestSuiteRunner(
    listOf(Paths.get("JSON-Schema-Test-Suite/tests/draft4/")),
    Paths.get("JSON-Schema-Test-Suite/remotes/"),
    Draft06RegressionTest::class.java.getResource(
        "/meta-schemas/schema-draft04.json"
    )!!,
    URI.create("http://localhost:1234/"),
    jsonSchemaType = JsonSchemaDraft04Type,
    schemaTestType = SchemaTestDraft04Type
)

class Draft04RegressionTest : RegressionTests() {
    override val runner = DRAFT04_RUNNER

    override val minimumTestCount = 321
}