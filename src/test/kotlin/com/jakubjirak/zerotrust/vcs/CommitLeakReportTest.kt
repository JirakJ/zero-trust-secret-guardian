package com.jakubjirak.zerotrust.vcs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommitLeakReportTest {

    private val secrets = mapOf("API" to "sk-live-12345", "DB" to "db-password-9", "FLAG" to "true")

    @Test
    fun `report lists files with the number of distinct secrets`() {
        val report = CommitLeakReport.build(
            mapOf(
                "b.env" to "API=sk-live-12345\nDB=db-password-9\nAPI2=sk-live-12345",
                "a.kt" to "val key = \"sk-live-12345\"",
                "clean.kt" to "val enabled = true",
            ),
            secrets
        )
        assertEquals(mapOf("a.kt" to 1, "b.env" to 2), report)
        assertEquals(listOf("a.kt", "b.env"), report.keys.toList())
    }

    @Test
    fun `empty report when nothing leaks`() {
        assertTrue(CommitLeakReport.build(mapOf("x.kt" to "fun main() {}"), secrets).isEmpty())
        assertTrue(CommitLeakReport.build(emptyMap(), secrets).isEmpty())
    }

    @Test
    fun `message names every leaking file`() {
        val message = CommitLeakReport.message(mapOf("a.kt" to 1, "b.env" to 2))
        assertTrue(message.contains("File: a.kt contains 1 known vault secret(s)"))
        assertTrue(message.contains("File: b.env contains 2 known vault secret(s)"))
        assertFalse(message.contains("sk-live"), "the message must not echo secret values")
    }

    @Test
    fun `directories and binary files are not scanned`() {
        assertFalse(CommitLeakReport.isScannable("lib.jar", false))
        assertFalse(CommitLeakReport.isScannable("Logo.PNG", false))
        assertFalse(CommitLeakReport.isScannable("src", true))
        assertTrue(CommitLeakReport.isScannable("Main.kt", false))
        assertTrue(CommitLeakReport.isScannable("Dockerfile", false))
    }
}
