package com.jakubjirak.zerotrust.execution

import com.jakubjirak.zerotrust.execution.SecretMasker.MASK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SecretMaskerTest {

    @Test
    fun `masks every occurrence of a secret`() {
        val masked = SecretMasker.mask("token=s3cr3t-value and again s3cr3t-value", mapOf("T" to "s3cr3t-value"))
        assertEquals("token=$MASK and again $MASK", masked)
    }

    @Test
    fun `returns null when nothing matches or input is empty`() {
        assertNull(SecretMasker.mask("plain log line", mapOf("T" to "s3cr3t-value")))
        assertNull(SecretMasker.mask("   ", mapOf("T" to "s3cr3t-value")))
        assertNull(SecretMasker.mask("s3cr3t-value", emptyMap()))
    }

    @Test
    fun `short values and common words are never masked`() {
        val secrets = mapOf("A" to "1234", "B" to "true", "C" to " PROD ", "D" to "dev")
        assertNull(SecretMasker.mask("1234 true PROD dev", secrets))
        assertFalse(SecretMasker.isMaskable("abcd"))
        assertTrue(SecretMasker.isMaskable("abcde"))
    }

    @Test
    fun `secret values are trimmed before matching`() {
        assertEquals("x=$MASK", SecretMasker.mask("x=password1", mapOf("P" to "  password1\n")))
    }

    @Test
    fun `longer secrets are masked before their substrings`() {
        val secrets = mapOf("SHORT" to "abcdef", "LONG" to "abcdef-ghijkl")
        assertEquals("v=$MASK", SecretMasker.mask("v=abcdef-ghijkl", secrets))
    }

    @Test
    fun `maskable values are distinct and sorted longest first`() {
        val values = SecretMasker.maskableValues(mapOf("A" to "aaaaa", "B" to "bbbbbbb", "C" to "aaaaa", "D" to "x"))
        assertEquals(listOf("bbbbbbb", "aaaaa"), values)
    }

    @Test
    fun `findLeaks lists only secrets present in content`() {
        val secrets = mapOf("A" to "alpha-secret", "B" to "beta-secret", "C" to "true")
        assertEquals(listOf("alpha-secret"), SecretMasker.findLeaks("val x = \"alpha-secret\" // true", secrets))
        assertTrue(SecretMasker.findLeaks("nothing here", secrets).isEmpty())
    }
}
