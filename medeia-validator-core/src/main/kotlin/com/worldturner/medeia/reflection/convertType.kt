package com.worldturner.medeia.reflection

import com.worldturner.medeia.parser.JsonTokenData
import com.worldturner.medeia.parser.JsonTokenType
import com.worldturner.medeia.parser.JsonTokenType.VALUE_BOOLEAN_FALSE
import com.worldturner.medeia.parser.JsonTokenType.VALUE_BOOLEAN_TRUE
import com.worldturner.medeia.parser.JsonTokenType.VALUE_NUMBER
import com.worldturner.medeia.parser.JsonTokenType.VALUE_TEXT
import com.worldturner.medeia.parser.TOKEN_FALSE
import com.worldturner.medeia.parser.TOKEN_NULL
import com.worldturner.medeia.parser.TOKEN_TRUE
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.util.EnumSet
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubclassOf

fun convertType(input: Any?, target: JsonTokenType): JsonTokenData =
    when (target) {
        VALUE_TEXT ->
            convertToText(input)
        VALUE_NUMBER ->
            convertToNumber(input)
        VALUE_BOOLEAN_FALSE, VALUE_BOOLEAN_TRUE ->
            convertToBoolean(input)
        else ->
            throw IllegalArgumentException("target=$target")
    }

fun convertType(input: Any?, target: KType): Any? {
    val classifier = target.classifier
    return if (classifier is KClass<*>) {
        when (input) {
            is String -> convertString(input, classifier)
            is Number -> convertNumber(input, classifier)
            is List<*> -> convertList(input, target)
            is Map<*, *> -> convertMap(input, target)
            else -> input
        }
    } else {
        input
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Enum<T>> isEnum(target: KType?): Class<T>? {
    val classifier = target?.classifier
    return if (classifier is KClass<*> && classifier.java.isEnum) {
        classifier.java as Class<T>
    } else {
        null
    }
}

val anyType = Any::class.createType()

fun convertMap(input: Map<*, *>, target: KType): Map<*, *> {
    return input.map { entry ->
        convertType(entry.key, target.arguments[0].type ?: anyType) to
            convertType(entry.value, target.arguments[1].type ?: anyType)
    }.toMap()
}

@Suppress("UNCHECKED_CAST")
fun <T : Enum<T>> buildEnumSet(input: Collection<*>, enumClass: Class<T>): EnumSet<*> {
    val result = EnumSet.noneOf<T>(enumClass)
    input.forEach { result.add(it as T) }
    return result
}

fun convertList(input: List<*>, target: KType): Collection<*> = run {
    val converted = input.map { convertType(it, target.arguments[0].type ?: anyType) }
    when (target.classifier) {
        List::class -> converted
        Set::class ->
            isEnum<Nothing>(target.arguments[0].type)?.let {
                buildEnumSet(converted, it)
            } ?: converted.toSet()
        EnumSet::class ->
            isEnum<Nothing>(target.arguments[0].type)?.let {
                buildEnumSet(converted, it)
            }!!
        else -> converted
    }
}

fun convertNumber(input: Number, target: KClass<*>) =
    when (target) {
        Int::class -> input.toInt()
        Long::class -> input.toLong()
        Float::class -> input.toFloat()
        Double::class -> input.toDouble()
        BigInteger::class -> {
            when (input) {
                is BigInteger -> input
                is BigDecimal -> input.toBigIntegerExact()
                else -> BigInteger.valueOf(input.toLong())
            }
        }
        else -> input
    }

fun convertString(input: String, target: KClass<*>): Any =
    when (target) {
        String::class -> input
        URI::class -> URI.create(input)
        Regex::class -> Regex(input)
        else -> when {
            target.isSubclassOf(Enum::class) -> createEnum(input, target)
            else -> input
        }
    }

fun convertToText(input: Any?): JsonTokenData = run {
    val text = input?.toString()
    if (text == null)
        TOKEN_NULL
    else
        JsonTokenData.createText(text)
}

fun convertToNumber(input: Any?): JsonTokenData = run {
    val number = input as Number?
    when (number) {
        null -> TOKEN_NULL
        is Int, is Short, is Byte ->
            JsonTokenData.createNumber(number.toLong())
        is Long ->
            JsonTokenData.createNumber(number)
        is BigInteger ->
            JsonTokenData(type = VALUE_NUMBER, integer = number)
        is BigDecimal ->
            JsonTokenData(type = VALUE_NUMBER, decimal = number)
        else ->
            throw IllegalArgumentException("Can't convert ${number::class} to number")
    }
}

fun convertToBoolean(input: Any?): JsonTokenData = run {
    val boolean = input as Boolean?
    if (boolean == null) TOKEN_NULL
    else if (boolean) TOKEN_TRUE
    else TOKEN_FALSE
}

fun createEnum(value: String, type: KClass<*>): Any {
    @Suppress("UNCHECKED_CAST")
    val constants = type.java.enumConstants as Array<Enum<*>>
    return constants.first { it.name.equals(value, ignoreCase = true) }
}