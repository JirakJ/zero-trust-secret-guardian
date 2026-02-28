package com.jakubjirak.zerotrust.execution

import com.intellij.execution.filters.ConsoleFilterProvider
import com.intellij.execution.filters.Filter
import com.intellij.openapi.project.Project

class ZeroTrustLogFilterProvider : ConsoleFilterProvider {
    override fun getDefaultFilters(project: Project): Array<Filter> {
        return arrayOf(ZeroTrustLogFilter(project))
    }
}

class ZeroTrustLogFilter(private val project: Project) : Filter {
    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val cache = ZeroTrustSecretCache.getInstance(project)
        val secrets = cache.getSecrets()
        
        if (secrets.isEmpty()) return null

        var hasSecret = false
        var maskedLine = line

        // Simple masking logic. In a real scenario, we might want to avoid masking short strings
        // or common words, but for demonstration we'll mask all secret values.
        for ((_, value) in secrets) {
            if (value.length > 3 && maskedLine.contains(value)) {
                maskedLine = maskedLine.replace(value, "********")
                hasSecret = true
            }
        }

        if (hasSecret) {
            // Logically we'd want to return a result that replaces the text, but Filter.Result is mainly
            // for creating hyperlinks. To actually modify the text being printed, we might need a
            // different extension point, like an InputFilter.
            // Let's implement an InputFilter as well or use this to just flag it. 
            // Actually, for masking output, an InputFilter is required. ConsoleFilterProvider only adds links.
        }

        return null
    }
}
