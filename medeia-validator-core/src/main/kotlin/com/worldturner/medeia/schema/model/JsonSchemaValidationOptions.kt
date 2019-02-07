package com.worldturner.medeia.schema.model

import com.worldturner.medeia.schema.model.UniqueItemsValidationMethod.DIGEST_MD5

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

data class JsonSchemaValidationOptions(
    val uniqueItemsValidationMethod: UniqueItemsValidationMethod = DIGEST_MD5,
    val optimizeExistentialValidators: Boolean = true
)