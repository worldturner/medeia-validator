import java.net.URLEncoder
import java.nio.charset.Charset

/**
 * Replace part of string from the last occurrence of given delimiter with the [replacement] string.
 * If the string does not contain the delimiter, returns [missingDelimiterValue] which defaults to the original string.
 */
fun String.replaceFromLast(delimiter: Char, replacement: String, missingDelimiterValue: String = this): String {
    val index = lastIndexOf(delimiter)
    return if (index == -1) missingDelimiterValue else replaceRange(index, length, replacement)
}

fun String.urlEncode() = URLEncoder.encode(this, Charsets.UTF_8.name())!!

@Suppress("NOTHING_TO_INLINE", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
inline fun CharSequence.toByteArray(charset: Charset = Charsets.UTF_8): ByteArray =
    (toString() as java.lang.String).getBytes(charset)