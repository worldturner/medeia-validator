package com.worldturner.medeia.schema

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.worldturner.medeia.parser.jackson.JacksonTokenDataWriter
import com.worldturner.medeia.parser.jackson.jsonFactory
import com.worldturner.medeia.parser.type.ArrayType
import com.worldturner.medeia.schema.model.TestResultType
import com.worldturner.medeia.schema.suite.DRAFT07_RUNNER
import com.worldturner.medeia.testing.support.JsonParserLibrary
import org.junit.Test
import java.io.Writer
import java.nio.file.Files
import java.nio.file.Paths

class ReportGeneratingTest {
    @Test
    fun `Run suite()`() {
        val runner = DRAFT07_RUNNER.copy(optional = false, filter = {
            true // it.endsWith("ref-into-non-schema.json")
        })

        val result =
            try {
//                    runner.testTree(null, false)
//                    runner.testStreamingGenerator(JsonParserLibrary.GSON)
                runner.testStreamingParser(JsonParserLibrary.JACKSON)
            } catch (e: JsonProcessingException) {
                System.err.println("Location: ${e.location?.sourceRef ?: "no location"}")
                throw e
            }
        println("Total tests:      ${result.size}")
        println("Successful tests: ${result.count { it.testSucceeded }}")
        println("Failed tests:     ${result.count { !it.testSucceeded }}")
        val testResultListType = ArrayType(TestResultType)
        createGenerator(Files.newBufferedWriter(Paths.get("target/test-report.json"))).use { generator ->
            val consumer = JacksonTokenDataWriter(generator)
            testResultListType.write(result, consumer)
        }
        createGenerator(Files.newBufferedWriter(Paths.get("target/failed-test-report.json"))).use { generator ->
            val consumer = JacksonTokenDataWriter(generator)
            val failed = result.filter { !it.testSucceeded }
            testResultListType.write(failed, consumer)
        }
    }

    fun createGenerator(writer: Writer) =
        jsonFactory.createGenerator(writer).setPrettyPrinter(DefaultPrettyPrinter())
}