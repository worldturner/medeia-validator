package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.schema.TestSuiteRunner
import java.net.URI
import java.nio.file.Paths

val DRAFT06_RUNNER = TestSuiteRunner(
    listOf(Paths.get("JSON-Schema-Test-Suite/tests/draft6/")),
    Paths.get("JSON-Schema-Test-Suite/remotes/"),
    Draft06RegressionTest::class.java.getResource(
        "/meta-schemas/schema-draft06.json"
    )!!,
    URI.create("http://localhost:1234/")
)

class Draft06RegressionTest : RegressionTests() {
    override val runner = DRAFT06_RUNNER

    override val minimumTestCount = 406
}