package com.worldturner.medeia.schema.performance

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.parser.jackson.JacksonTokenDataJsonParser
import com.worldturner.medeia.parser.jackson.jsonFactory
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.ValidationBuilderContext
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import com.worldturner.medeia.schema.validation.stream.SchemaValidatorInstance
import com.worldturner.medeia.testing.support.JsonParserLibrary
import com.worldturner.medeia.testing.support.parse
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import java.io.BufferedInputStream
import java.nio.file.Files
import java.nio.file.Path

abstract class PerformanceTest(val iterations: Int) {

    abstract fun run(dataPath: Path): Boolean

    fun runWithTiming(dataPath: Path): Double {
        val start = System.nanoTime()
        for (i in 0..iterations) {
            if (!run(dataPath)) {
                println("Validation failed for $this")
            }
        }
        val end = System.nanoTime()
        return (end - start).toDouble() / iterations / 1_000_000.0
    }
}

class JsonNodeValidatorPerformanceTest(schemaPath: Path, iterations: Int) : PerformanceTest(iterations) {
    val schemaTree = Files.newBufferedReader(schemaPath).use { testMapper.readTree(it) }
    val factory = JsonSchemaFactory.byDefault()
    val schema = factory.getJsonSchema(schemaTree)

    override fun run(dataPath: Path): Boolean {
        Files.newBufferedReader(dataPath).use { input ->
            val dataTree = testMapper.readTree(input)
            val report: ProcessingReport = schema.validate(dataTree)
            return report.isSuccess
        }
    }

    companion object {
        internal val testMapper = ObjectMapper()
    }
}

class MedeiaJacksonPerformanceTest(schemaPath: Path, iterations: Int, schemaType: MapperType = JsonSchemaDraft04Type) :
    PerformanceTest(iterations) {
    val schema =
        parse(schemaType, Files.newInputStream(schemaPath), JsonParserLibrary.JACKSON) as JsonSchema
    val validator = schema.buildValidator(ValidationBuilderContext())

    override fun run(dataPath: Path): Boolean {
        val validatorInstance: SchemaValidatorInstance = validator.createInstance()
        val consumer = SchemaValidatingConsumer(validatorInstance)
        BufferedInputStream(Files.newInputStream(dataPath)).use { input ->
            val parser = JacksonTokenDataJsonParser(consumer, jsonFactory.createParser(input))
            return try {
                parser.parseAll()
                true
            } catch (e: ValidationFailedException) {
                println("Exception: $e")
                false
            }
        }
    }
}

class MedeiaGsonPerformanceTest(schemaPath: Path, iterations: Int, schemaType: MapperType = JsonSchemaDraft04Type) :
    PerformanceTest(iterations) {
    val schema =
        parse(schemaType, Files.newInputStream(schemaPath), JsonParserLibrary.GSON) as JsonSchema
    val validator = schema.buildValidator(ValidationBuilderContext())

    override fun run(dataPath: Path): Boolean {
        val validatorInstance: SchemaValidatorInstance = validator.createInstance()
        val consumer = SchemaValidatingConsumer(validatorInstance)
        Files.newBufferedReader(dataPath).use { input ->
            val parser = GsonJsonReaderDecorator(input, consumer)
            return try {
                parser.parseAll()
                true
            } catch (e: ValidationFailedException) {
                println("Exception: $e")
                false
            }
        }
    }
}

class EveritPerformanceTest(schemaPath: Path, iterations: Int) : PerformanceTest(iterations) {
    val schemaTree = Files.newBufferedReader(schemaPath).use {
        JSONObject(JSONTokener(it))
    }
    val schema = SchemaLoader.load(schemaTree)

    override fun run(dataPath: Path): Boolean {
        Files.newBufferedReader(dataPath).use { input ->
            val jsonObject = JSONObject(JSONTokener(input))
            return try {
                schema.validate(jsonObject)
                true
            } catch (e: ValidationException) {
                println("Validation Exception for ${this::class.java.simpleName}: $e")
                false
            }
        }
    }
}
