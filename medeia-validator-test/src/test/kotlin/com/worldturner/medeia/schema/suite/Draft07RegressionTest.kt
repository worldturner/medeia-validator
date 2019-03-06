package com.worldturner.medeia.schema.suite

import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.MetaSchemaSource
import java.net.URI
import java.nio.file.Paths

val DRAFT07_RUNNER
    get() = TestSuiteRunner(
        listOf(
            Paths.get("JSON-Schema-Test-Suite/tests/draft7/"),
            Paths.get("Additional-Test-Suite/draft7/")
        ),
        Paths.get("JSON-Schema-Test-Suite/remotes/"),
        MetaSchemaSource.DRAFT07,
        URI.create("http://localhost:1234/"),
        version = JsonSchemaVersion.DRAFT07
    )

class Draft07RegressionTest : RegressionTests() {
    override val runner = DRAFT07_RUNNER
    override val minimumTestCount = 425
    override val minimumOptionalTestCount = 1
}