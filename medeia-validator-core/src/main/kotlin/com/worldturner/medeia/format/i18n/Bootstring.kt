package com.worldturner.medeia.format.i18n

import com.worldturner.util.toCodePoints

// TODO: character case encoding (optional)
// TODO: allowed character rules from https://tools.ietf.org/html/rfc5892

object Bootstring {
    fun encode(s: String, p: BootstringParameters): String {
        val builder = StringBuilder()
        var n = p.initialN
        var delta = 0
        var bias = p.initialBias
        val codePoints = s.toCodePoints()
        val b = codePoints.count { p.isBasicCodePoint(it) }
        var h = b
        codePoints.forEach {
            if (p.isBasicCodePoint(it)) builder.appendCodePoint(it)
            else if (it < p.initialN) throw IllegalArgumentException()
        }
        if (b > 0) builder.appendCodePoint(p.delimiter)
        while (h < codePoints.size) {
            // let m = the minimum {non-basic} code point >= n in the input
            val m = codePoints.minIf { it >= n }!!
            // let delta = delta + (m - n) * (h + 1), fail on overflow
            delta += (m - n).toInt() * (h + 1)
            // let n = m
            n = m
            // for each code point c in the input (in order) do begin
            for (c in codePoints) {
                // if c < n {or c is basic} then increment delta, fail on overflow
                if (c < n || p.isBasicCodePoint(c)) delta++
                // if c == n then begin
                else if (c == n) {
                    // let q = delta
                    var q = delta
                    // for k = base to infinity in steps of base do begin
                    var k = p.base
                    while (true) {
                        // let t = tmin if k <= bias {+ tmin}, or
                        //         tmax if k >= bias + tmax, or k - bias otherwise
                        val t = (k - bias).restrictRange(p.tmin, p.tmax)
                        // if q < t then break
                        if (q < t) break
                        // output the code point for digit t + ((q - t) mod (base - t))
                        val digit = t + ((q - t).rem(p.base - t))
                        builder.appendCodePoint(p.digitToBasicCodePoint(digit))
                        // let q = (q - t) div (base - t)
                        q = (q - t) / (p.base - t)
                        // end
                        k += p.base
                    }
                    // output the code point for digit q
                    builder.appendCodePoint(p.digitToBasicCodePoint(q))
                    // let bias = adapt(delta, h + 1, test h equals b?)
                    bias = adapt(delta, h + 1, h == b, p)
                    // let delta = 0
                    delta = 0
                    // increment h
                    h++
                    // end
                }
                // end
            }
            // increment delta and n
            delta++
            n++
            // end
        }
        return builder.toString()
    }

    private fun adapt(delta: Int, numPoints: Int, firstTime: Boolean, p: BootstringParameters): Int {
        // if firsttime then let delta = delta div damp
        // else let delta = delta div 2
        var workingDelta =
            if (firstTime) delta / p.damp
            else delta / 2
        // let delta = delta + (delta div numpoints)
        workingDelta += workingDelta / numPoints
        // let k = 0
        var k = 0
        // while delta > ((base - tmin) * tmax) div 2 do begin
        while (workingDelta > ((p.base - p.tmin) * p.tmax) / 2) {
            // let delta = delta div(base - tmin)
            workingDelta /= p.base - p.tmin
            // let k = k +base
            k += p.base
            // end
        }
        // return k + (((base - tmin + 1) * delta) div (delta + skew))
        return k + (((p.base - p.tmin + 1) * workingDelta) / (workingDelta + p.skew))
    }
}

class BootstringParameters(
    val base: Int,
    val tmin: Int,
    val tmax: Int,
    val skew: Int,
    val damp: Int,
    val initialBias: Int,
    val initialN: Int,
    val delimiter: Int,
    val basicCodePoints: String
) {
    private val basicCodePointArray = IntArray(initialN.toInt()) {
        basicCodePoints.indexOf(it.toChar().toLowerCase())
    }

    fun isBasicCodePoint(ch: Int) = ch < initialN && basicCodePointArray[ch.toInt()] >= 0
    fun basicCodePointValueToDigit(ch: Int) =
        if (ch < initialN)
            basicCodePointArray[ch.toInt()].also {
                if (it < 0) throw IllegalArgumentException("Codepoint $ch is not a basic code point")
            }
        else
            throw IllegalArgumentException("Codepoint $ch is not a basic code point")

    fun digitToBasicCodePoint(d: Int) =
        basicCodePoints.codePointAt(d)
}

private fun Int.restrictRange(min: Int, max: Int): Int =
    if (this < min) min else if (this > max) max else this

private fun Collection<Int>.minIf(filter: (Int) -> Boolean): Int? {
    if (isEmpty()) return null
    var min = Integer.MAX_VALUE
    var valid = false
    for (e in this) {
        if (filter(e) && e <= min) {
            min = e
            valid = true
        }
    }
    return if (valid) min else null
}