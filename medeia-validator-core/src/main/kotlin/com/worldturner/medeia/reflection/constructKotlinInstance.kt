package com.worldturner.medeia.reflection

import com.worldturner.medeia.parser.JsonTokenData
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

fun constructKotlinInstance(kotlinClass: KClass<*>, kotlinArguments: Map<String, Any?>, lastToken: JsonTokenData): Any {
    return constructKotlinInstance(kotlinClass, kotlinClass.constructors, kotlinArguments, lastToken)
}

fun constructKotlinInstance(kotlinClass: KClass<*>, kotlinConstructors: Collection<KFunction<Any>>, kotlinArguments: Map<String, Any?>, lastToken: JsonTokenData): Any {
    val constructorsWithRequiredParameters = kotlinConstructors.filter { constructor ->
        val parameterNames = constructor.parameters.map { it.name }
        kotlinArguments.keys.subtract(parameterNames).isEmpty()
    }
    val constructorArgs =
            constructorsWithRequiredParameters.mapNotNull wholeConstructor@{ constructor ->
                constructor to constructor.parameters.mapNotNull { parameter ->
                    if (parameter.name in kotlinArguments) {
                        val argument = convertType(kotlinArguments[parameter.name], parameter.type)
                        parameter to argument
                    } else if (parameter.isOptional) {
                        null
                    } else {
                        return@wholeConstructor null
                    }
                }.toMap()
            }
    if (constructorArgs.isEmpty()) {
        throw IllegalArgumentException(
                "No kotlinConstructors found for $kotlinClass that can accept just ${kotlinArguments.keys} at $lastToken")
    }
    val constructorArgWithLeastUnusedParameters =
            constructorArgs.sortedBy { (constructor, arguments) ->
                constructor.parameters.size - arguments.size
            }.first()

    constructorArgWithLeastUnusedParameters.let {
        try {
            return it.first.callBy(it.second)
        } catch (e: IllegalArgumentException) {
            val arguments = it.second.map {
                "${it.key.name}: ${it.value?.let { it::class.qualifiedName } ?: "null"}"
            }
            throw IllegalArgumentException(
                    "Error during reflective construction of $kotlinClass, provided parameters: " +
                            "$arguments at $lastToken", e)
        }
    }
}