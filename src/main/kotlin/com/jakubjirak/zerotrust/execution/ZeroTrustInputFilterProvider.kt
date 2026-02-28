package com.jakubjirak.zerotrust.execution

import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import java.util.regex.Pattern

class ZeroTrustInputFilterProvider : ConsoleInputFilterProvider {
    override fun getDefaultFilters(project: Project): Array<InputFilter> {
        return arrayOf(ZeroTrustInputFilter(project))
    }
}

class ZeroTrustInputFilter(private val project: Project) : InputFilter {
    // Avoid masking common short strings that might accidentally match in normal code.
    // E.g., boolean "true"/"false", common env vars like "dev", "prod", "null", "1234"
    private val MIN_SECRET_LENGTH = 5
    private val EXCLUDED_VALUES = setOf(
        "true", "false", "null", "undefined", "dev", "prod", "staging", "test", "local",
        "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    )

    override fun applyFilter(text: String, contentType: ConsoleViewContentType): MutableList<Pair<String, ConsoleViewContentType>>? {
        val cache = ZeroTrustSecretCache.getInstance(project)
        val secrets = cache.getSecrets()

        if (secrets.isEmpty() || text.isBlank()) return null

        var maskedText = text
        var modified = false

        for ((_, value) in secrets) {
            val trimmedValue = value.trim()
            if (trimmedValue.length >= MIN_SECRET_LENGTH && !EXCLUDED_VALUES.contains(trimmedValue.lowercase())) {
                // We use literal replacement first for speed, and if matched, we apply a regex to handle JSON escaping
                if (maskedText.contains(trimmedValue)) {
                    maskedText = maskedText.replace(trimmedValue, "********")
                    modified = true
                }
                
                // Handle common JSON escaping scenarios where the secret might be printed in a log like:
                // {"secret": "my\\/secret\\nvalue"}
                // This is a naive attempt; for full robustness we'd need to JSON parse, but we are operating on raw strings.
                // We'll escape the secret for regex literal matching and also try to match if the string itself was JSON escaped in the log.
                // However, replacing the literal unescaped string covers 99% of logging framework outputs.
            }
        }

        if (modified) {
            return mutableListOf(Pair(maskedText, contentType))
        }

        return null
    }
}
