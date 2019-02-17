package com.worldturner.medeia.api

import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT04
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT07
import com.worldturner.medeia.parser.ArrayNodeData
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.NodeData
import com.worldturner.medeia.parser.ObjectNodeData
import com.worldturner.medeia.parser.SimpleObjectMapper
import com.worldturner.medeia.parser.SimpleTreeBuilder
import com.worldturner.medeia.parser.TokenNodeData
import com.worldturner.medeia.parser.tree.JsonParserFromSimpleTree
import com.worldturner.medeia.parser.type.MapperType
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.schema.EMPTY_URI
import com.worldturner.medeia.schema.hasFragment
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.Schema
import com.worldturner.medeia.schema.model.SchemaWithBaseUri
import com.worldturner.medeia.schema.model.ValidationBuilderContext
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.parser.JsonSchemaDraft07Type
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.withEmptyFragment
import com.worldturner.medeia.schema.withoutFragment
import java.io.IOException
import java.io.Writer
import java.net.URI
import java.net.URISyntaxException

private val JsonSchemaVersion.mapperType: MapperType
    get() =
        when (this) {
            DRAFT04 -> JsonSchemaDraft04Type
            DRAFT07 -> JsonSchemaDraft07Type
        }

/* To avoid infinite loops in case there is a subtle bug in Medeia $ref resolution. */
private const val MAX_REF_RESOLVE_ITERATIONS = 100

abstract class MedeiaApiBase {

    fun loadSchemas(sources: List<SchemaSource>, options: JsonSchemaValidationOptions) =
        loadSchemas(sources, validatorMap = null, options = options)

    @JvmOverloads
    fun loadSchemas(
        sources: List<SchemaSource>,
        validatorMap: MutableMap<URI, SchemaValidator>? = null,
        options: JsonSchemaValidationOptions = JsonSchemaValidationOptions.DEFAULT
    ): SchemaValidator {
        if (sources.isEmpty())
            throw IllegalArgumentException("Need at least one schema source")
        val schemaIds = mutableMapOf<URI, NodeData>()
        val parsedSchemas = sources.map { loadSchema(it, schemaIds) }
        val validators = buildValidators(parsedSchemas, options, schemaIds, validatorMap)
        return validators.first()
    }

    private fun buildValidators(
        parsedSchemas: List<Schema>,
        options: JsonSchemaValidationOptions,
        schemaIds: MutableMap<URI, NodeData>,
        validatorMap: MutableMap<URI, SchemaValidator>?
    ): List<SchemaValidator> {
        val context = ValidationBuilderContext(options = options)
        val validators = parsedSchemas.map { it.buildValidator(context) }
        val extraValidators = mutableListOf<SchemaValidator>()
        // Keep resolving unknown ref until none can be found anymore
        // (but limit to avoid turning a small bug into a hang of Medeia)
        for (iteration in 1..MAX_REF_RESOLVE_ITERATIONS) {
            // TODO: maybe we can remove the recursive recordUnknownRefs visitor methods and
            // TODO: instead only have RefIdValidator implement them, if all of them are in
            // TODO: the context.schemaValidatorsById map
            val unknownRefs = mutableSetOf<URI>()
            validators.forEach { it.recordUnknownRefs(unknownRefs) }
            extraValidators.forEach { it.recordUnknownRefs(unknownRefs) }
            var refFound = false
            unknownRefs.forEach { absoluteRef ->
                val node = findNode(schemaIds, absoluteRef)
                if (node == null) {
                    println("Unknown \$ref $absoluteRef not found")
                }
                node?.let {
                    val validator = parseSchemaFromNode(
                        node,
                        context.withBaseUri(absoluteRef, root = true)
                    )
                    extraValidators += validator
                    refFound = true
                }

            }
            // As long as refs are found, they themselves could contain unknown refs
            if (!refFound) break
        }
        validatorMap?.let {
            it.putAll(context.schemaValidatorsById)
        }
        return validators
    }

    fun convertSchemaToDraft07(source: SchemaSource, destination: Writer) {
        val schema = loadSchema(source)
        val consumer = createTokenDataConsumerWriter(destination)
        JsonSchemaDraft04Type.write(schema, consumer)
    }

    protected abstract fun createSchemaParser(
        source: SchemaSource,
        consumer: JsonTokenDataAndLocationConsumer
    ): JsonParserAdapter

    protected abstract fun createTokenDataConsumerWriter(destination: Writer): JsonTokenDataConsumer

    private fun loadSchema(source: SchemaSource, ids: MutableMap<URI, NodeData> = mutableMapOf()): Schema {
        val tree = parseTree(source)
        tree.collectIds(source.baseUri, ids)
        source.baseUri?.let { ids[it] = tree }
        val consumer = SimpleObjectMapper(source.version.mapperType, 0)
        val parser: JsonParserAdapter = JsonParserFromSimpleTree(tree, consumer)
        parser.parseAll()
        val schema = consumer.takeResult() as JsonSchema
        val augmentedSchema = source.baseUri?.let { SchemaWithBaseUri(it, schema) } ?: schema
        return augmentedSchema
    }

    private fun parseTree(source: SchemaSource): NodeData {
        try {
            val consumer = SimpleTreeBuilder(0)
            val parser: JsonParserAdapter = createSchemaParser(source, consumer)
            parser.parseAll()
            return consumer.takeResult() as NodeData
        } catch (e: IOException) {
            throw Exception("In file with baseUri ${source.baseUri}", e)
        }
    }
}

private fun parseSchemaFromNode(node: NodeData, context: ValidationBuilderContext): SchemaValidator {
    val mapperType = JsonSchemaVersion.DRAFT07.mapperType // TODO: get mappertype for NodeData from file
    val consumer = SimpleObjectMapper(mapperType, 0)
    val parser: JsonParserAdapter = JsonParserFromSimpleTree(node, consumer)
    parser.parseAll()
    val schema = consumer.takeResult() as JsonSchema
    return schema.buildValidator(context)
}

private fun findNode(nodeMap: MutableMap<URI, NodeData>, ref: URI): NodeData? {
    // First step: look up ids
    nodeMap[ref]?.let { return it }
    return if (ref.hasFragment()) {
        // Next step - look up json pointer relative to id
        val pointer = JsonPointer(ref.fragment)
        val baseNode = nodeMap[ref.withEmptyFragment()] ?: nodeMap[ref.withoutFragment()]
        val targetNode = baseNode?.let { baseNode.resolve(pointer) }
        targetNode
    } else {
        null
    }
}

private fun NodeData.collectIds(baseUri: URI?, ids: MutableMap<URI, NodeData>) {
    val baseUri = (registerAndGetJsonSchemaId(baseUri, ids) ?: EMPTY_URI).also {
        // Force register the root even if it isn't an object node with an $id
        ids[it] = this
    }
    collectIdsNonRoot(baseUri, ids)
}

private fun NodeData.collectIdsNonRoot(baseUri: URI?, ids: MutableMap<URI, NodeData>) {
    when (this) {
        is ObjectNodeData -> {
            val newBaseUri = registerAndGetJsonSchemaId(baseUri, ids) ?: baseUri
            nodes.values.forEach { it.collectIdsNonRoot(newBaseUri, ids) }
        }
        is ArrayNodeData -> nodes.forEach { it.collectIdsNonRoot(baseUri, ids) }
    }
}

fun NodeData.registerAndGetJsonSchemaId(baseUri: URI?, ids: MutableMap<URI, NodeData>): URI? =
    if (this is ObjectNodeData) {
        val idNode = nodes["\$id"]
        if (idNode is TokenNodeData && idNode.token.type == JsonTokenType.VALUE_TEXT) {
            try {
                val relativeUri = URI(idNode.token.text!!)
                val absoluteUri = baseUri?.let { baseUri.resolve(relativeUri) } ?: relativeUri
                absoluteUri.also { ids[absoluteUri] = this }
            } catch (e: URISyntaxException) {
                // TODO: maybe worth a debug log
                baseUri
            }
        } else {
            baseUri
        }
    } else {
        null
    }

fun NodeData.resolve(pointer: JsonPointer): NodeData? {
    val firstName = pointer.firstName()
    val selected =
        when (this) {
            is ObjectNodeData -> nodes[firstName]
            is ArrayNodeData -> try {
                nodes[firstName.toInt()]
            } catch (e: NumberFormatException) {
                null
            }
            is TokenNodeData -> if (firstName.isEmpty()) this else null
        }
    val tail = pointer.tail()
    return if (tail != null) selected?.resolve(tail) else selected
}