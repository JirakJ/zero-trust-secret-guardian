package com.jakubjirak.zerotrust.execution

/**
 * Pure masking logic shared by console output masking and the pre-commit scan.
 *
 * Very short values and common words (`true`, `dev`, digits, …) are never treated
 * as secrets, otherwise ordinary log lines would be garbled.
 */
object SecretMasker {
    const val MASK = "********"
    const val MIN_SECRET_LENGTH = 5

    private val EXCLUDED_VALUES = setOf(
        "true", "false", "null", "undefined", "dev", "prod", "staging", "test", "local",
    )

    /** True if [value] is long and distinctive enough to be masked / scanned for. */
    fun isMaskable(value: String): Boolean {
        val trimmed = value.trim()
        return trimmed.length >= MIN_SECRET_LENGTH && trimmed.lowercase() !in EXCLUDED_VALUES
    }

    /** The maskable secret values, longest first so a secret containing another is masked whole. */
    fun maskableValues(secrets: Map<String, String>): List<String> =
        secrets.values.map { it.trim() }.filter { isMaskable(it) }.distinct().sortedByDescending { it.length }

    /** Returns [text] with every maskable secret replaced by [MASK], or `null` when nothing matched. */
    fun mask(text: String, secrets: Map<String, String>): String? {
        if (text.isBlank() || secrets.isEmpty()) return null
        var masked = text
        for (value in maskableValues(secrets)) {
            masked = masked.replace(value, MASK)
        }
        return masked.takeIf { it != text }
    }

    /** Values from [secrets] that literally occur in [content]. */
    fun findLeaks(content: String, secrets: Map<String, String>): List<String> =
        maskableValues(secrets).filter { content.contains(it) }
}
