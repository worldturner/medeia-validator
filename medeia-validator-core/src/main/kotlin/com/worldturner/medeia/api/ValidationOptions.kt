package com.worldturner.medeia.api

import com.worldturner.medeia.api.UniqueItemsValidationMethod.DIGEST_MD5

/**
 * Method to use for validating unique items in array.
 *
 * IN_MEMORY_TREES is the only method that 100% guarantees no false positives or negatives, but it builds
 * in-memory trees for all Json structures so it is not a good approach for validating large medeia documents
 * in a streaming fashion.
 *
 * The other methods don't have any false negatives - if the method says that there are no duplicate items,
 * then this is guaranteed to be correct. However there is a possibility of false positives, in which a duplicate
 * is detected when there is no duplicate in reality. The chance of this is very low though.
 *
 * Important note: for most normal applications, security isn't a concern.
 *
 * The reason for using MessageDigest for hashing is that cryptographic
 * hashes give a really good distribution of values for any input.
 *
 * It is normally of no concern that a hash collision can be forced by an attacker.
 * (But please don't make an application in which an important decision is based
 * on whether or not this validator detects a unique item validation error in medeia source,
 * or not)
 *
 * The fastest MessageDigest implementation that has good mixing is the one to use.
 */
enum class UniqueItemsValidationMethod(
    val digest: Boolean,
    val algorithm: String
) {
    DIGEST_MD5(true, "MD5"),
    DIGEST_SHA1(true, "SHA"),
    DIGEST_SHA256(true, "SHA-256"),
    DIGEST_SHA512(true, "SHA-512"),
    IN_MEMORY_TREES(false, "n/a"),
}

/**
 * A custom format validation function.
 * @param value the value to be validated, having the following types:
 *              java.lang.String for a JSON string value
 *              java.lang.Boolean for a JSON boolean value
 *              null for a JSON null value
 *              a java.lang.Number subclass for a numeric value;
 *              java.lang.Integer/java.lang.Long/java.math.BigInteger for integral values
 *              java.lang.Double/java.lang.BigDecimal for floating point values
 * @param format the name of the format being validated
 * @return null if the validation was successful, or an error message if it was not successful.
 */
interface FormatValidation {
    fun validate(value: Any?, format: String): String?
}

data class JsonSchemaValidationOptions @JvmOverloads constructor(
    val uniqueItemsValidationMethod: UniqueItemsValidationMethod = DIGEST_MD5,
    val optimizeExistentialValidators: Boolean = true,
    /** Whether to validate (best effort) based on the "format" keyword. */
    val validateFormat: Boolean = true,
    /** Whether to validate (best effort) based on the "contentEncoding" and "contentMediaEncoding" keywords. */
    val validateContent: Boolean = true,
    /**
     * According to the specifications, $ref refs can point anywhere, even places that weren't considered
     * schemas before the $ref appeared. This is seldom necessary (put named schemas in the "definitions" section)
     * and schema reading is faster without this.
     */
    val supportRefsToAnywhere: Boolean = true,
    /** Custom formats for "format" keyword". Custom formats can override built-in formats. */
    val customFormats: Map<String, FormatValidation> = emptyMap()
) {
    fun withUniqueItemsValidationMethod(value: UniqueItemsValidationMethod) =
        copy(uniqueItemsValidationMethod = value)

    fun withOptimizeExistentialValidators(value: Boolean) =
        copy(optimizeExistentialValidators = value)

    fun withValidateFormat(value: Boolean) =
        copy(validateFormat = value)

    fun withValidateContent(value: Boolean) =
        copy(validateContent = value)

    fun withCustomFormats(customFormats: Map<String, FormatValidation>) =
        copy(customFormats = customFormats)

        companion object {
        @JvmField
        val DEFAULT = JsonSchemaValidationOptions()
    }
}