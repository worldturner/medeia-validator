package com.worldturner.medeia.api

import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT04
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT06
import com.worldturner.medeia.api.JsonSchemaVersion.DRAFT07
import com.worldturner.medeia.parser.ArrayNode
import com.worldturner.medeia.parser.JsonParserAdapter
import com.worldturner.medeia.parser.JsonTokenDataAndLocationConsumer
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.MultipleConsumer
import com.worldturner.medeia.parser.TreeNode
import com.worldturner.medeia.parser.ObjectNode
import com.worldturner.medeia.parser.SimpleObjectMapper
import com.worldturner.medeia.parser.SimpleTreeBuilder
import com.worldturner.medeia.parser.SimpleNode
import com.worldturner.medeia.parser.tree.JsonParserFromSimpleTree
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.schema.model.JsonSchema
import com.worldturner.medeia.schema.model.Schema
import com.worldturner.medeia.schema.model.SchemaWithBaseUri
import com.worldturner.medeia.schema.model.ValidationBuilderContext
import com.worldturner.medeia.schema.parser.JsonSchemaDraft04Type
import com.worldturner.medeia.schema.validation.SchemaValidator
import com.worldturner.medeia.schema.validation.stream.SchemaValidatingConsumer
import com.worldturner.util.EMPTY_URI
import com.worldturner.util.hasFragment
import com.worldturner.util.withEmptyFragment
import com.worldturner.util.withoutFragment
import java.io.IOException
import java.io.Writer
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.ConcurrentHashMap

private val JsonSchemaVersion.idProperty: String
    get() =
        when (this) {
            DRAFT04 -> "id"
            DRAFT06, DRAFT07 -> "\$id"
        }

private const val URI_DRAFT04 = "http://json-schema.org/draft-04/schema#"
private const val URI_DRAFT06 = "http://json-schema.org/draft-06/schema#"
private const val URI_DRAFT07 = "http://json-schema.org/draft-07/schema#"

/* Note: being lenient in allowing schema without trailing #. */
private val schemaUriToVersionMapping = mapOf(
    URI_DRAFT04.removeSuffix("#") to DRAFT04,
    URI_DRAFT04 to DRAFT04,
    URI_DRAFT06.removeSuffix("#") to DRAFT06,
    URI_DRAFT06 to DRAFT06,
    URI_DRAFT07.removeSuffix("#") to DRAFT07,
    URI_DRAFT07 to DRAFT07
)

/* To avoid infinite loops in case there is a subtle bug in Medeia $ref resolution. */
private const val MAX_REF_RESOLVE_ITERATIONS = 100

abstract class MedeiaApiBase {
    private val metaSchemaValidators: MutableMap<JsonSchemaVersion, SchemaValidator> = ConcurrentHashMap()

    fun loadSchemas(sources: List<SchemaSource>, options: ValidationOptions) =
        loadSchemas(sources, validatorMap = null, options = options)

    fun loadSchema(source: SchemaSource) = loadSchemas(listOf(source))

    @JvmOverloads
    fun loadSchemas(
        sources: List<SchemaSource>,
        validatorMap: MutableMap<URI, SchemaValidator>? = null,
        options: ValidationOptions = ValidationOptions.DEFAULT
    ): SchemaValidator {
        if (sources.isEmpty())
            throw IllegalArgumentException("Need at least one schema source")
        val schemaIds = mutableMapOf<URI, VersionedTree>()
        val parsedSchemas = sources.map { loadSchema(it, options, schemaIds) }
        val validators = buildValidators(parsedSchemas, options, schemaIds, validatorMap)
        return validators.first()
    }

    /**
     * Loads meta schema validator or cached version if already there.
     */
    private fun loadMetaSchemaValidator(version: JsonSchemaVersion): SchemaValidator {
        return metaSchemaValidators.computeIfAbsent(version) {
            loadSchemas(listOf(MetaSchemaSource.forVersion(version)), ValidationOptions(validateSchema = false))
        }
    }

    private fun buildValidators(
        parsedSchemas: List<Schema>,
        options: ValidationOptions,
        schemaIds: MutableMap<URI, VersionedTree>,
        validatorMap: MutableMap<URI, SchemaValidator>?
    ): List<SchemaValidator> {
        val context = ValidationBuilderContext(options = options)
        val validators = parsedSchemas.map { it.buildValidator(context) }

        val unknownRefs =
            if (options.supportRefsToAnywhere) {
                findRefsToAnywhere(validators, schemaIds, context)
            } else {
                mutableSetOf<URI>().also { set ->
                    validators.forEach { it.recordUnknownRefs(set) }
                }
            }

        if (unknownRefs.isNotEmpty()) {
            throw IllegalArgumentException(
                "Invalid schema combination, unresolved \$ref references: ${unknownRefs.joinToString()}"
            )
        }

        validatorMap?.let {
            it.putAll(context.schemaValidatorsById)
        }

        return validators
    }

    /*
     * Returns unknown $ref refs after attempted resolution.
     */
    private fun findRefsToAnywhere(
        validators: List<SchemaValidator>,
        schemaIds: MutableMap<URI, VersionedTree>,
        context: ValidationBuilderContext
    ): Set<URI> {
        val extraValidators = mutableListOf<SchemaValidator>()
        var unknownRefs: MutableSet<URI>? = null
        // Keep resolving unknown ref until none can be found anymore
        // (but limit to avoid turning a small bug into a hang of Medeia)
        for (iteration in 1..MAX_REF_RESOLVE_ITERATIONS) {
            // TODO: maybe we can remove the recursive recordUnknownRefs visitor methods and
            // TODO: instead only have RefIdValidator implement them, if all of them are in
            // TODO: the context.schemaValidatorsById map
            unknownRefs = mutableSetOf<URI>()
            validators.forEach { it.recordUnknownRefs(unknownRefs) }
            extraValidators.forEach { it.recordUnknownRefs(unknownRefs) }
            var refFound = false
            unknownRefs.forEach { absoluteRef ->
                val node = findNode(schemaIds, absoluteRef)
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
        return unknownRefs ?: emptySet()
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

    private fun loadSchema(
        source: SchemaSource,
        options: ValidationOptions,
        ids: MutableMap<URI, VersionedTree> = mutableMapOf()
    ): Schema {
        val tree = parseIntoTree(source)

        if (options.supportRefsToAnywhere) {
            tree.collectIds(source.baseUri, ids)
            source.baseUri?.let { ids[it] = tree }
        }
        val schemaBuilder = SimpleObjectMapper(tree.version.mapperType, 0)
        parseTreeIntoJsonSchema(schemaBuilder, tree, options)

        val schema = schemaBuilder.takeResult() as JsonSchema
        return source.baseUri?.let { SchemaWithBaseUri(it, schema) } ?: schema
    }

    private fun parseTreeIntoJsonSchema(
        schemaBuilder: SimpleObjectMapper,
        tree: VersionedTree,
        options: ValidationOptions
    ) {
        // If meta-schema validation option is on, then validate the schema while parsing it
        val consumer =
            if (options.validateSchema) {
                MultipleConsumer(
                    listOf(
                        SchemaValidatingConsumer(loadMetaSchemaValidator(tree.version)),
                        schemaBuilder
                    )
                )
            } else {
                schemaBuilder
            }
        JsonParserFromSimpleTree(tree.tree, consumer).use { it.parseAll() }
    }

    private fun parseIntoTree(source: SchemaSource): VersionedTree {
        try {
            val consumer = SimpleTreeBuilder(0)
            val parser: JsonParserAdapter = createSchemaParser(source, consumer)
            parser.use { it.parseAll() }
            val tree = consumer.takeResult() as TreeNode
            val schemaUri = tree.textProperty("\$schema")
            val version =
                schemaUriToVersionMapping[schemaUri] ?: source.version
                ?: throw IllegalArgumentException(
                    (schemaUri?.let { "Version specified \"$schemaUri\" is not known in $source" }
                        ?: "Version not specified in schema $source") +
                        ", modify schema or pass version in SchemaSource.version"
                )
            return VersionedTree(tree, version)
        } catch (e: IOException) {
            throw Exception("In file with baseUri ${source.baseUri}", e)
        }
    }
}

private fun parseSchemaFromNode(node: VersionedTree, context: ValidationBuilderContext): SchemaValidator {
    val mapperType = node.version.mapperType
    val consumer = SimpleObjectMapper(mapperType, 0)
    val parser: JsonParserAdapter = JsonParserFromSimpleTree(node.tree, consumer)
    parser.parseAll()
    val schema = consumer.takeResult() as JsonSchema
    return schema.buildValidator(context)
}

private fun findNode(nodeMap: MutableMap<URI, VersionedTree>, ref: URI): VersionedTree? {
    // First step: look up ids
    nodeMap[ref]?.let { return it }
    return if (ref.hasFragment()) {
        // Next step - look up json pointer relative to id
        val pointer = JsonPointer(ref.fragment)
        val baseNode = nodeMap[ref.withEmptyFragment()] ?: nodeMap[ref.withoutFragment()]
        val targetNode = baseNode?.let {
            it.tree.resolve(pointer)?.let { VersionedTree(it, baseNode.version) }
        }
        targetNode
    } else {
        null
    }
}

private fun VersionedTree.collectIds(baseUri: URI?, ids: MutableMap<URI, VersionedTree>) {
    val newBaseUri = (tree.registerAndGetJsonSchemaId(baseUri, ids, version) ?: EMPTY_URI).also {
        // Force register the root even if it isn't an object node with an $id
        ids[it] = this
    }
    tree.collectIdsNonRoot(newBaseUri, ids, version)
}

private fun TreeNode.collectIdsNonRoot(
    baseUri: URI?,
    ids: MutableMap<URI, VersionedTree>,
    version: JsonSchemaVersion
) {
    when (this) {
        is ObjectNode -> {
            val newBaseUri = registerAndGetJsonSchemaId(baseUri, ids, version) ?: baseUri
            nodes.values.forEach { it.collectIdsNonRoot(newBaseUri, ids, version) }
        }
        is ArrayNode -> nodes.forEach { it.collectIdsNonRoot(baseUri, ids, version) }
        else -> {
        }
    }
}

private fun TreeNode.registerAndGetJsonSchemaId(
    baseUri: URI?,
    ids: MutableMap<URI, VersionedTree>,
    version: JsonSchemaVersion
): URI? =
    textProperty(version.idProperty)?.let {
        try {
            val relativeUri = URI(it.toString())
            val absoluteUri = baseUri?.let { baseUri.resolve(relativeUri) } ?: relativeUri
            absoluteUri.also { ids[absoluteUri] = VersionedTree(this, version) }
        } catch (e: URISyntaxException) {
            // TODO: maybe worth a debug log
            baseUri
        }
    } ?: baseUri

fun TreeNode.resolve(pointer: JsonPointer): TreeNode? {
    val firstName = pointer.firstName()
    val selected =
        when (this) {
            is ObjectNode -> nodes[firstName]
            is ArrayNode -> try {
                nodes[firstName.toInt()]
            } catch (e: NumberFormatException) {
                null
            }
            is SimpleNode -> if (firstName.isEmpty()) this else null
        }
    val tail = pointer.tail()
    return if (tail != null) selected?.resolve(tail) else selected
}

internal data class VersionedTree(val tree: TreeNode, val version: JsonSchemaVersion)