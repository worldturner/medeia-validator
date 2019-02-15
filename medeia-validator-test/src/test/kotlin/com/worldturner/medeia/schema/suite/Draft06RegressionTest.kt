package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.api.JsonSchemaVersion
import java.net.URI
import java.nio.file.Paths

val DRAFT06_RUNNER
    get() = TestSuiteRunner(
        listOf(Paths.get("JSON-Schema-Test-Suite/tests/draft6/")),
        Paths.get("JSON-Schema-Test-Suite/remotes/"),
        Draft06RegressionTest::class.java.getResource(
            "/meta-schemas/schema-draft06.json"
        )!!,
        URI.create("http://localhost:1234/"),
        version = JsonSchemaVersion.DRAFT07
    )

class Draft06RegressionTest : RegressionTests() {
    override val runner = DRAFT06_RUNNER

    override val minimumTestCount = 406
}