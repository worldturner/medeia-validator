package com.worldturner.medeia.format

import com.worldturner.medeia.format.i18n.Punycode
import org.junit.Test
import kotlin.test.assertEquals

class PunycodeBootstringTest {

    /**
     * (A) Arabic (Egyptian)
     */
    @Test
    fun testSpecA() {
        assertEquals(
            "egbpdaj6bu4bxfgehfvwxn",
            Punycode.encode(
                "\u0644\u064A\u0647\u0645\u0627\u0628\u062A\u0643\u0644" +
                    "\u0645\u0648\u0634\u0639\u0631\u0628\u064A\u061F"
            )
        )
    }

    /**
     * (B) Chinese (simplified)
     */
    @Test
    fun testSpecB() {
        assertEquals(
            "ihqwcrb4cv8a8dqg056pqjye",
            Punycode.encode("\u4ED6\u4EEC\u4E3A\u4EC0\u4E48\u4E0D\u8BF4\u4E2D\u6587")
        )
    }

    /**
     * (C) Chinese (traditional)
     */
    @Test
    fun testSpecC() {
        assertEquals(
            "ihqwctvzc91f659drss3x8bo0yb",
            Punycode.encode("\u4ED6\u5011\u7232\u4EC0\u9EBD\u4E0D\u8AAA\u4E2D\u6587")
        )
    }

    /**
     * (D) Czech: Pro<ccaron>prost<ecaron>nemluv<iacute><ccaron>esky
     */
    @Test
    fun testSpecD() {
        assertEquals(
            "Proprostnemluvesky-uyb24dma41a",
            Punycode.encode(
                "\u0050\u0072\u006F\u010D\u0070\u0072\u006F\u0073\u0074" +
                    "\u011B\u006E\u0065\u006D\u006C\u0075\u0076\u00ED\u010D" +
                    "\u0065\u0073\u006B\u0079"
            )
        )
    }

    /**
     * (E) Hebrew
     */
    @Test
    fun testSpecE() {
        assertEquals(
            "4dbcagdahymbxekheh6e0a7fei0b",
            Punycode.encode(
                "\u05DC\u05DE\u05D4\u05D4\u05DD\u05E4\u05E9\u05D5\u05D8" +
                    "\u05DC\u05D0\u05DE\u05D3\u05D1\u05E8\u05D9\u05DD\u05E2" +
                    "\u05D1\u05E8\u05D9\u05EA"
            )
        )
    }

    /**
     * (F) Hindi (Devanagari)
     */
    @Test
    fun testSpecF() {
        assertEquals(
            "i1baa7eci9glrd9b2ae1bj0hfcgg6iyaf8o0a1dig0cd",
            Punycode.encode(
                "\u092F\u0939\u0932\u094B\u0917\u0939\u093F\u0928\u094D" +
                    "\u0926\u0940\u0915\u094D\u092F\u094B\u0902\u0928\u0939" +
                    "\u0940\u0902\u092C\u094B\u0932\u0938\u0915\u0924\u0947" +
                    "\u0939\u0948\u0902"
            )
        )
    }

    /**
     * (G) Japanese (kanji and hiragana):
     */
    @Test
    fun testSpecG() {
        assertEquals(
            "n8jok5ay5dzabd5bym9f0cm5685rrjetr6pdxa",
            Punycode.encode(
                "\u306A\u305C\u307F\u3093\u306A\u65E5\u672C\u8A9E\u3092" +
                    "\u8A71\u3057\u3066\u304F\u308C\u306A\u3044\u306E\u304B"
            )
        )
    }

    /**
     * (R) <sono><supiido><de>
     */
    @Test
    fun testSpecR() {
        assertEquals(
            "d9juau41awczczp",
            Punycode.encode("\u305D\u306E\u30B9\u30D4\u30FC\u30C9\u3067")
        )
    }

    /**
     * Testing "😂" - all non BMP (Basic Plane) characters with codepoints > 65536.
     */
    @Test
    fun testNonBMP1() {
        val s = "\uD83D\uDE02"
        assertEquals("g28h", Punycode.encode(s))
    }

    /**
     * Testing "𝒯𝒜😂𝔹" - all non BMP (Basic Plane) characters with codepoints > 65536.
     */
    @Test
    fun testNonBMP2() {
        assertEquals(
            "521hfbx2a369h",
            Punycode.encode("\uD835\uDCAF\uD835\uDC9C\uD83D\uDE02\uD835\uDD39")
        )
    }

    @Test
    fun testBasicCodePoints() {
        assertEquals("abc-", Punycode.encode("abc"))
    }
}
