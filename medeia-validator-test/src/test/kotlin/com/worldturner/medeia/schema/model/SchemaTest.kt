package com.worldturner.medeia.schema.model

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.google.gson.Gson
import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.JsonSchemaVersion
import com.worldturner.medeia.api.MedeiaApiBase
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.api.StringSchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationOptions
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.gson.toJsonElement
import com.worldturner.medeia.jackson.toTreeNode
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.parser.gson.GsonJsonWriterDecorator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonGenerator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonParser
import com.worldturner.medeia.parser.jackson.jsonFactory
import com.worldturner.medeia.parser.jackson.mapper
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.util.NullWriter
import java.io.StringReader
import java.nio.file.Path

val options = ValidationOptions()

data class SchemaTest(
    val description: String,
    val schema: String,
    val tests: List<SchemaTestCase>,
    val path: Path? = null,
    val remotePaths: Set<SchemaSource> = emptySet()
) {
    fun validator(medeiaApiBase: MedeiaApiBase, version: JsonSchemaVersion) =
        medeiaApiBase.loadSchemas(
            listOf(StringSchemaSource(schema, version = version)) + remotePaths
        )

    fun withRemotes(remotes: Set<SchemaSource>) = copy(remotePaths = remotes)

    fun testStreamingGenerator(
        medeiaApiBase: MedeiaApiBase,
        version: JsonSchemaVersion,
        library: JsonParserLibrary
    ) =
        tests.map { it.testStreamingGenerator(this, medeiaApiBase, version, library) }

    fun testStreamingParser(
        medeiaApiBase: MedeiaApiBase,
        version: JsonSchemaVersion,
        library: JsonParserLibrary
    ) =
        tests.map { it.testStreamingParser(this, medeiaApiBase, version, library) }
}

data class SchemaTestCase(
    val description: String,
    val data: TreeNode,
    val valid: Boolean
) {
    val dataAsString = data.toString()
    val dataAsJacksonTree = data.toTreeNode()
    val dataAsGsonTree = data.toJsonElement()

    val jacksonFactory = JsonFactory()

    fun testStreamingGenerator(
        schemaTest: SchemaTest,
        medeiaApiBase: MedeiaApiBase,
        version: JsonSchemaVersion,
        library: JsonParserLibrary
    ): TestResult {
        val writer = NullWriter()
        val validator = schemaTest.validator(medeiaApiBase, version)
        when (library) {
            JsonParserLibrary.JACKSON -> {
                val generator = JacksonTokenDataJsonGenerator(
                    jacksonFactory.createGenerator(writer),
                    SchemaValidatingConsumer(validator),
                    null
                )
                try {
                    mapper.writeTree(generator, dataAsJacksonTree)
                } catch (e: JsonMappingException) {
                    e.cause.let {
                        if (it is ValidationFailedException) {
                            return TestResult(schemaTest, this, it.failures.toSingleResult())
                        } else {
                            throw e
                        }
                    }
                }
                return TestResult(schemaTest, this, OkValidationResult)
            }
            JsonParserLibrary.GSON -> {
                val gsonWriter = GsonJsonWriterDecorator(
                    writer,
                    SchemaValidatingConsumer(validator),
                    inputSourceName = null
                )
                try {
                    Gson().toJson(dataAsGsonTree, gsonWriter)
                } catch (e: ValidationFailedException) {
                    return TestResult(schemaTest, this, e.failures.toSingleResult())
                }
                return TestResult(schemaTest, this, OkValidationResult)
            }
        }
    }

    fun testStreamingParser(
        schemaTest: SchemaTest,
        medeiaApiBase: MedeiaApiBase,
        version: JsonSchemaVersion,
        library: JsonParserLibrary
    ): TestResult {
        val consumer = SchemaValidatingConsumer(schemaTest.validator(medeiaApiBase, version))
        val parser = createParser(dataAsString, consumer, library)
        try {
            parser.parseAll()
        } catch (e: ValidationFailedException) {
            return TestResult(schemaTest, this, e.failures.toSingleResult())
        } catch (e: JsonParseException) {
            System.err.println("In $dataAsString")
            throw e
        }
        return TestResult(schemaTest, this, OkValidationResult)
    }

    private fun createParser(
        input: String,
        consumer: JsonTokenDataAndLocationConsumer,
        library: JsonParserLibrary
    ): JsonParserAdapter =
        when (library) {
            JsonParserLibrary.JACKSON -> JacksonTokenDataJsonParser(
                jsonFactory.createParser(input),
                consumer,
                inputSourceName = description
            )
            JsonParserLibrary.GSON -> GsonJsonReaderDecorator(
                StringReader(input),
                consumer,
                inputSourceName = description
            )
        }
}

data class TestResult(
    val test: SchemaTest,
    val case: SchemaTestCase,
    val outcome: ValidationResult
) {
    val testSucceeded get() = outcome.valid == case.valid
}

fun List<FailedValidationResult>.toSingleResult() =
    FailedValidationResult(rule = "multiple", message = "Multiple", location = "", details = this)