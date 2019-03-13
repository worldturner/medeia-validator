package com.worldturner.medeia.schema.performance

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.worldturner.medeia.api.InputSource
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT04
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT06
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT07
import com.worldturner.medeia.api.SchemaSource
import com.worldturner.medeia.api.ValidationFailedException
import com.worldturner.medeia.api.gson.MedeiaGsonApi
import com.worldturner.medeia.api.jackson.MedeiaJacksonApi
import com.worldturner.medeia.parser.gson.GsonJsonReaderDecorator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import org.json.JSONTokener
import org.leadpony.justify.api.JsonValidationService
import java.io.BufferedReader
import java.io.StringReader

abstract class PerformanceTest(
    val iterations: Int
) {
    abstract fun run(): Boolean

    fun runWithTiming(): Double =
        timing(iterations) {
            if (!run()) {
                println("Validation failed for $this")
            }
        }
}

class JsonNodeValidatorPerformanceTest(
    schemaSource: SchemaSource,
    dataSource: InputSource,
    iterations: Int
) : PerformanceTest(iterations) {
    val schemaTree = BufferedReader(schemaSource.input.reader).use { testMapper.readTree(it) }
    val factory = JsonSchemaFactory.byDefault()
    val schema = factory.getJsonSchema(schemaTree)
    val data = String(dataSource.stream.use { it.readBytes() }, Charsets.UTF_8)

    override fun run(): Boolean {
        val dataTree = testMapper.readTree(data)
        val report: ProcessingReport = schema.validate(dataTree)
        return report.isSuccess
    }

    companion object {
        internal val testMapper = ObjectMapper()
        val supports = setOf(DRAFT04)
    }
}

class MedeiaJacksonPerformanceTest(
    schemaSource: SchemaSource,
    dataSource: InputSource,
    iterations: Int
) : PerformanceTest(iterations) {
    val api = MedeiaJacksonApi()
    val validator = api.loadSchema(schemaSource)
    val data = String(dataSource.stream.use { it.readBytes() }, Charsets.UTF_8)

    override fun run(): Boolean {
        val parser = api.decorateJsonParser(validator, api.jsonFactory.createParser(data))
        return try {
            while (parser.nextToken() != null) {
            }
            true
        } catch (e: ValidationFailedException) {
            println("Exception: $e")
            false
        }
    }

    companion object {
        val supports = setOf(DRAFT04, DRAFT06, DRAFT07)
    }
}

class MedeiaGsonPerformanceTest(
    schemaSource: SchemaSource,
    dataSource: InputSource,
    iterations: Int
) : PerformanceTest(iterations) {
    val api = MedeiaGsonApi()
    val validator = api.loadSchema(schemaSource)
    val data = String(dataSource.stream.use { it.readBytes() }, Charsets.UTF_8)

    override fun run(): Boolean {
        val consumer = SchemaValidatingConsumer(validator)
        val parser = GsonJsonReaderDecorator(StringReader(data), consumer)
        return try {
            parser.parseAll()
            true
        } catch (e: ValidationFailedException) {
            println("Exception: $e")
            false
        }
    }

    companion object {
        val supports = setOf(DRAFT04, DRAFT06, DRAFT07)
    }
}

class EveritPerformanceTest(
    schemaSource: SchemaSource,
    dataSource: InputSource,
    iterations: Int
) : PerformanceTest(iterations) {
    val schemaTree = BufferedReader(schemaSource.input.reader).use {
        JSONObject(JSONTokener(it))
    }
    val schema = SchemaLoader.load(schemaTree)
    val data = String(dataSource.stream.use { it.readBytes() }, Charsets.UTF_8)

    override fun run(): Boolean {
        val jsonObject = JSONObject(JSONTokener(data))
        return try {
            schema.validate(jsonObject)
            true
        } catch (e: ValidationException) {
            println("Validation Exception for ${this::class.java.simpleName}: $e")
            false
        }
    }

    companion object {
        val supports = setOf(DRAFT04, DRAFT06, DRAFT07)
    }
}

class JustifyPerformanceTest(
    schemaSource: SchemaSource,
    dataSource: InputSource,
    iterations: Int
) : PerformanceTest(iterations) {
    val schemaTree = BufferedReader(schemaSource.input.reader).use {
        JSONObject(JSONTokener(it))
    }
    val service = JsonValidationService.newInstance()
    val schema = BufferedReader(schemaSource.input.reader).use { service.readSchema(it) }
    val data = String(dataSource.stream.use { it.readBytes() }, Charsets.UTF_8)

    override fun run(): Boolean {
        val problemHandler = service.createProblemPrinter(::println)
        // Parses the JSON instance by javax.json.stream.JsonParser
        service.createParser(StringReader(data), schema, problemHandler).use { parser ->
            while (parser.hasNext()) {
                parser.next()
            }
        }
        return true
    }

    companion object {
        val supports = setOf(DRAFT07)
    }
}
