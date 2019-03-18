package com.worldturner.medeia.parser.type

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenDataConsumer
import com.worldturner.medeia.parser.JsonTokenLocation
import com.worldturner.medeia.parser.JsonTokenType.END_OBJECT
import com.worldturner.medeia.parser.JsonTokenType.FIELD_NAME
import com.worldturner.medeia.parser.JsonTokenType.START_OBJECT
import com.worldturner.medeia.parser.TOKEN_END_OBJECT
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_START_OBJECT
import com.worldturner.medeia.parser.builder.ObjectValueBuilder
import com.worldturner.medeia.parser.type.AcceptKind.NOT_ACCEPTED
import com.worldturner.medeia.parser.type.AcceptKind.STRUCTURE
import com.worldturner.medeia.pointer.JsonPointer
import com.worldturner.medeia.reflection.constructKotlinInstance
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class PropertyType(
    val propertyName: String,
    val type: MapperType,
    val kotlinPropertyName: String = propertyName,
    val readOnly: Boolean = false
)

open class ObjectType(
    val kotlinClass: KClass<*>,
    propertyTypes: List<PropertyType>,
    val additionalPropertiesType: MapperType? = null,
    val kotlinAdditionalPropertiesProperty: String? = null,
    val ignoreAdditionalProperties: Boolean = false,
    val kotlinJsonPointerProperty: String? = null,
    val kotlinConstructors: Collection<KFunction<Any>>? = null
) : StructuredType() {

    val propertyTypeMap: Map<String, PropertyType> = propertyTypes.map { it.propertyName to it }.toMap()

    fun itemType(propertyName: String): MapperType =
        propertyTypeMap[propertyName]?.type ?: if (additionalPropertiesType != null)
            additionalPropertiesType
        else if (ignoreAdditionalProperties)
            UnknownType
        else
            throw IllegalArgumentException("Unknown property $propertyName")

    override fun accepts(token: JsonTokenData) =
        if (token.type == START_OBJECT) STRUCTURE else NOT_ACCEPTED

    override fun createBuilder(token: JsonTokenData, location: JsonTokenLocation) =
        ObjectValueBuilder(location.level, kotlinJsonPointerProperty?.let { location.pointer }, this)

    fun createValue(lastToken: JsonTokenData, pointer: JsonPointer?, input: Any): Any {
        @Suppress("UNCHECKED_CAST")
        val arguments = input as Map<String, Any>
        val additionalProperties = arguments
            .filter { (k, _) -> k !in propertyTypeMap }
        val kotlinArguments = arguments
            .filterKeys { it in propertyTypeMap }.asSequence()
            .map { entry -> propertyTypeMap[entry.key]?.kotlinPropertyName?.let { it to entry.value } }
            .plus(kotlinAdditionalPropertiesProperty?.let {
                kotlinAdditionalPropertiesProperty to additionalProperties
            })
            .plus(kotlinJsonPointerProperty?.let {
                kotlinJsonPointerProperty to pointer
            })
            .filterNotNull()
            .toMap()
        return kotlinConstructors?.let {
            constructKotlinInstance(kotlinClass, kotlinConstructors, kotlinArguments, lastToken)
        } ?: constructKotlinInstance(kotlinClass, kotlinArguments, lastToken)
    }

    override fun isComplete(token: JsonTokenData): Boolean = token.type == END_OBJECT

    override fun write(value: Any?, consumer: JsonTokenDataConsumer) {
        if (value == null) {
            consumer.consume(TOKEN_NULL)
        } else {
            val kotlinProperties = prepareProperties(value)
            writeObject(value, kotlinProperties, consumer)
        }
    }

    fun writeObject(
        value: Any,
        kotlinProperties: Map<String, KProperty1<Any, *>>,
        consumer: JsonTokenDataConsumer
    ) {
        consumer.consume(TOKEN_START_OBJECT)
        propertyTypeMap.values.forEach { propertyType ->
            if (!propertyType.readOnly) {
                val propertyValue = reflectProperty(value, kotlinProperties, propertyType.kotlinPropertyName)
                if (propertyValue != null) {
                    consumer.consume(
                        JsonTokenData(type = FIELD_NAME, text = propertyType.propertyName)
                    )
                    propertyType.type.write(propertyValue, consumer)
                }
            }
        }
        consumer.consume(TOKEN_END_OBJECT)
    }
}

fun <T : Any> prepareProperties(value: T): Map<String, KProperty1<T, *>> = run {
    @Suppress("UNCHECKED_CAST")
    val memberProperties =
        value::class.memberProperties as Collection<KProperty1<T, Any?>>
    memberProperties.map { it.name to it }.toMap()
}

fun <T : Any> reflectProperty(
    value: T,
    kotlinProperties: Map<String, KProperty1<T, *>>,
    kotlinPropertyName: String
): Any? = run {
    val kotlinProperty = kotlinProperties[kotlinPropertyName]
        ?: throw IllegalArgumentException(
            "Can't find property '$kotlinPropertyName' on object of type ${value::class}"
        )

    kotlinProperty.get(value)
}
