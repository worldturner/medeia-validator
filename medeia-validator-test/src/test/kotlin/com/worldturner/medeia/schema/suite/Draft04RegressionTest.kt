package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.MetaSchemaSource
import java.net.URI
import java.nio.file.Paths

val DRAFT04_RUNNER
    get() = TestSuiteRunner(
        listOf(Paths.get("JSON-Schema-Test-Suite/tests/draft4/")),
        Paths.get("JSON-Schema-Test-Suite/remotes/"),
        MetaSchemaSource(version = JsonSchemaVersion.DRAFT04),
        URI.create("http://localhost:1234/"),
        version = JsonSchemaVersion.DRAFT04
    )

class Draft04RegressionTest : RegressionTests() {
    override val runner = DRAFT04_RUNNER

    override val minimumTestCount = 321
}