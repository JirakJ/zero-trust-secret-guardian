package com.jakubjirak.zerotrust.execution

import com.intellij.execution.filters.ConsoleInputFilterProvider
import com.intellij.execution.filters.InputFilter
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair

/** Masks injected vault secrets in Run/Debug console output. */
class ZeroTrustInputFilterProvider : ConsoleInputFilterProvider {
    override fun getDefaultFilters(project: Project): Array<InputFilter> {
        return arrayOf(ZeroTrustInputFilter(project))
    }
}

class ZeroTrustInputFilter(private val project: Project) : InputFilter {

    override fun applyFilter(text: String, contentType: ConsoleViewContentType): MutableList<Pair<String, ConsoleViewContentType>>? {
        val secrets = ZeroTrustSecretCache.getInstance(project).getSecrets()
        val masked = SecretMasker.mask(text, secrets) ?: return null
        return mutableListOf(Pair(masked, contentType))
    }
}
