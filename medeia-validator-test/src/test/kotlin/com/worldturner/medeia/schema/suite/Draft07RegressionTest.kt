package com.worldturner.medeia.schema.suite

import java.net.URI
import java.nio.file.Paths

val DRAFT07_RUNNER = TestSuiteRunner(
    listOf(
        Paths.get("JSON-Schema-Test-Suite/tests/draft7/"),
        Paths.get("Additional-Test-Suite/draft7/")
    ),
    Paths.get("JSON-Schema-Test-Suite/remotes/"),
    Draft07RegressionTest::class.java.getResource(
        "/meta-schemas/schema-draft07.json"
    )!!,
    URI.create("http://localhost:1234/")
)

class Draft07RegressionTest : RegressionTests() {
    override val runner = DRAFT07_RUNNER
    override val minimumTestCount = 425
    override val minimumOptionalTestCount = 1
}