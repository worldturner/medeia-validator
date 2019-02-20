package com.worldturner.medeia.format

import com.worldturner.medeia.format.IdnProperty.CONTEXTO
import com.worldturner.medeia.format.IdnProperty.DISALLOWED
import com.worldturner.medeia.format.IdnProperty.PVALID
import com.worldturner.medeia.format.i18n.Punycode
import com.worldturner.util.toCodePoints
import java.lang.Character.UnicodeBlock.ANCIENT_GREEK_MUSICAL_NOTATION
import java.lang.Character.UnicodeBlock.COMBINING_MARKS_FOR_SYMBOLS
import java.lang.Character.UnicodeBlock.MUSICAL_SYMBOLS

fun String.isIdnHostname() =
    try {
        this.idnToLdhHostname().isHostname()
    } catch (e: IllegalArgumentException) {
        false
    }

fun String.idnToLdhHostname(): String =
    this.split('.').map { it.idnToLdhLabel() }.joinToString(".")

fun String.idnToLdhLabel(): String =
    if (this.toCodePoints().count { Punycode.parameters.isBasicCodePoint(it) } == this.length) {
        // Label is not internationalized
        this
    } else {
        this.toCodePoints().forEach {
            if (it.idnProperty() != PVALID)
                throw IllegalArgumentException("Illegal code point in IDN label $it")
        }
        "xn--${Punycode.encode(this)}"
    }

enum class IdnProperty {
    PVALID, CONTEXTJ, CONTEXTO, DISALLOWED, UNASSIGNED
}

fun Int.idnProperty(): IdnProperty {
    //    If .cp. .in.  Exceptions Then Exceptions(cp);
    idnCodePointsExceptions[this]?.let { return it }
    //    Else If .cp. .in.  BackwardCompatible Then BackwardCompatible(cp);
    idnCodePointsBackwardsCompatible[this]?.let { return it }
    //    Else If .cp. .in.  Unassigned Then UNASSIGNED;
    //    Else If .cp. .in.  LDH Then PVALID;
    if (this == '-'.toInt() || Punycode.parameters.isBasicCodePoint(this)) return PVALID
    //    Else If .cp. .in.  JoinControl Then CONTEXTJ;
    //    Else If .cp. .in.  Unstable Then DISALLOWED;
    //    Else If .cp. .in.  IgnorableProperties Then DISALLOWED;
    if (idnIsIgnorableProperty()) return DISALLOWED
    //    Else If .cp. .in.  IgnorableBlocks Then DISALLOWED;
    if (idnIsIgnorableBlocks()) return DISALLOWED
    //    Else If .cp. .in.  OldHangulJamo Then DISALLOWED;
    //    Else If .cp. .in.  LetterDigits Then PVALID;
    if (Character.isLetterOrDigit(this)) return PVALID
    //    Else DISALLOWED;
    return DISALLOWED
}

fun Int.idnIsIgnorableProperty(): Boolean {
    return Character.isWhitespace(this)
    // C: Default_Ignorable_Code_Point(cp) = True or
    //    White_Space(cp) = True or
    //    Noncharacter_Code_Point(cp) = True
}

fun Int.idnIsIgnorableBlocks(): Boolean {
    // D: Block(cp) is in {Combining Diacritical Marks for Symbols,
    //                     Musical Symbols, Ancient Greek Musical Notation}
    val block = Character.UnicodeBlock.of(this)
    return block == COMBINING_MARKS_FOR_SYMBOLS || block == MUSICAL_SYMBOLS || block == ANCIENT_GREEK_MUSICAL_NOTATION
}

val idnCodePointsExceptions = mapOf(
    0x00DF to PVALID, // LATIN SMALL LETTER SHARP S
    0x03C2 to PVALID, // GREEK SMALL LETTER FINAL SIGMA
    0x06FD to PVALID, // ARABIC SIGN SINDHI AMPERSAND
    0x06FE to PVALID, // ARABIC SIGN SINDHI POSTPOSITION MEN
    0x0F0B to PVALID, // TIBETAN MARK INTERSYLLABIC TSHEG
    0x3007 to PVALID, // IDEOGRAPHIC NUMBER ZERO
    0x00B7 to CONTEXTO, // MIDDLE DOT
    0x0375 to CONTEXTO, // GREEK LOWER NUMERAL SIGN (KERAIA)
    0x05F3 to CONTEXTO, // HEBREW PUNCTUATION GERESH
    0x05F4 to CONTEXTO, // HEBREW PUNCTUATION GERSHAYIM
    0x30FB to CONTEXTO, // KATAKANA MIDDLE DOT
    0x0660 to CONTEXTO, // ARABIC-INDIC DIGIT ZERO
    0x0661 to CONTEXTO, // ARABIC-INDIC DIGIT ONE
    0x0662 to CONTEXTO, // ARABIC-INDIC DIGIT TWO
    0x0663 to CONTEXTO, // ARABIC-INDIC DIGIT THREE
    0x0664 to CONTEXTO, // ARABIC-INDIC DIGIT FOUR
    0x0665 to CONTEXTO, // ARABIC-INDIC DIGIT FIVE
    0x0666 to CONTEXTO, // ARABIC-INDIC DIGIT SIX
    0x0667 to CONTEXTO, // ARABIC-INDIC DIGIT SEVEN
    0x0668 to CONTEXTO, // ARABIC-INDIC DIGIT EIGHT
    0x0669 to CONTEXTO, // ARABIC-INDIC DIGIT NINE
    0x06F0 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT ZERO
    0x06F1 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT ONE
    0x06F2 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT TWO
    0x06F3 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT THREE
    0x06F4 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT FOUR
    0x06F5 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT FIVE
    0x06F6 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT SIX
    0x06F7 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT SEVEN
    0x06F8 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT EIGHT
    0x06F9 to CONTEXTO, // EXTENDED ARABIC-INDIC DIGIT NINE
    0x0640 to DISALLOWED, // ARABIC TATWEEL
    0x07FA to DISALLOWED, // NKO LAJANYALAN
    0x302E to DISALLOWED, // HANGUL SINGLE DOT TONE MARK
    0x302F to DISALLOWED, // HANGUL DOUBLE DOT TONE MARK
    0x3031 to DISALLOWED, // VERTICAL KANA REPEAT MARK
    0x3032 to DISALLOWED, // VERTICAL KANA REPEAT WITH VOICED SOUND MARK
    0x3033 to DISALLOWED, // VERTICAL KANA REPEAT MARK UPPER HALF
    0x3034 to DISALLOWED, // VERTICAL KANA REPEAT WITH VOICED SOUND MARK UPPER HA
    0x3035 to DISALLOWED, // VERTICAL KANA REPEAT MARK LOWER HALF
    0x303B to DISALLOWED // VERTICAL IDEOGRAPHIC ITERATION MARK
)

val idnCodePointsBackwardsCompatible = mapOf<Int, IdnProperty>()