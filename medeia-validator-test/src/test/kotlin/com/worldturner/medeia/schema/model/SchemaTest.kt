package com.worldturner.medeia.schema.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.google.gson.Gson
import com.worldturner.medeia.api.FailedValidationResult
import com.worldturner.medeia.api.JsonSchemaValidationOptions
import com.worldturner.medeia.api.OkValidationResult
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.ValidationResult
import com.worldturner.medeia.gson.toJsonElement
import com.worldturner.medeia.jackson.toTreeNode
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.parser.gson.GsonJsonWriterDecorator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonGenerator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonParser
import com.worldturner.medeia.parser.jackson.jsonFactory
import com.worldturner.medeia.parser.jackson.mapper
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.util.NullWriter
import java.io.StringReader
import java.nio.file.Path

val options = JsonSchemaValidationOptions()

data class SchemaTest(
    val description: String,
    @JsonIgnore
    val schema: JsonSchema,
    val tests: List<SchemaTestCase>,
    val path: Path? = null,
    @JsonIgnore
    val remotes: Set<Schema> = emptySet()
) {
    @JsonIgnore
    val schemaSet = JsonSchemaSet(main = schema, additional = remotes)
    @JsonIgnore
    val validator = schemaSet.buildValidator(ValidationBuilderContext(options = options))

    fun withRemotes(remotes: Set<Schema>) = copy(remotes = remotes)

    fun testStreamingGenerator(library: JsonParserLibrary) = tests
        .map { it.testStreamingGenerator(this, library) }

    fun testStreamingParser(library: JsonParserLibrary) = tests
        .map { it.testStreamingParser(this, library) }
}

data class SchemaTestCase(
    val description: String,
    val data: NodeData,
    val valid: Boolean
) {
    val dataAsString = data.toString()
    val dataAsJacksonTree = data.toTreeNode()
    val dataAsGsonTree = data.toJsonElement()

    val jacksonFactory = JsonFactory()

    fun testStreamingGenerator(schemaTest: SchemaTest, library: JsonParserLibrary): TestResult {
        val writer = NullWriter()
        val validatorInstance: SchemaValidatorInstance = schemaTest.validator.createInstance(0)

        when (library) {
            JsonParserLibrary.JACKSON -> {
                val generator = JacksonTokenDataJsonGenerator(
                    SchemaValidatingConsumer(validatorInstance),
                    jacksonFactory.createGenerator(writer)
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
                    SchemaValidatingConsumer(validatorInstance)
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

    fun testStreamingParser(schemaTest: SchemaTest, library: JsonParserLibrary): TestResult {
        val validatorInstance: SchemaValidatorInstance = schemaTest.validator.createInstance(0)
        val consumer = SchemaValidatingConsumer(validatorInstance)
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
            JsonParserLibrary.JACKSON -> JacksonTokenDataJsonParser(consumer, jsonFactory.createParser(input))
            JsonParserLibrary.GSON -> GsonJsonReaderDecorator(StringReader(input), consumer)
        }
}

data class TestResult(
    @get:JsonIgnoreProperties("tests")
    val test: SchemaTest,
    val case: SchemaTestCase,
    val outcome: ValidationResult
) {
    val testSucceeded get() = outcome.valid == case.valid
}

fun List<FailedValidationResult>.toSingleResult() =
    FailedValidationResult(rule = "multiple", message = "Multiple", location = "", details = this)