package com.jakubjirak.zerotrust.vcs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readText
import com.intellij.openapi.ui.Messages
import com.jakubjirak.zerotrust.execution.ZeroTrustSecretCache
import com.jakubjirak.zerotrust.settings.VaultSettings
import com.jakubjirak.zerotrust.api.VaultClientFactory
import com.jakubjirak.zerotrust.licensing.LicenseManager
import kotlinx.coroutines.runBlocking

class ZeroTrustCheckinHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return ZeroTrustCheckinHandler(panel.project, panel)
    }
}

class ZeroTrustCheckinHandler(
    private val project: Project,
    private val panel: CheckinProjectPanel
) : CheckinHandler() {

    override fun beforeCheckin(): ReturnResult {
        // Pre-commit scanning is an Enterprise feature
        if (!LicenseManager.getInstance(project).isEnterprise()) {
            return ReturnResult.COMMIT
        }

        // Fetch secrets to check against. We use cache or fetch from vault if cache is empty.
        var secrets = ZeroTrustSecretCache.getInstance(project).getSecrets().values.filter { it.length > 3 }
        
        if (secrets.isEmpty()) {
            // Attempt to load from vault settings if cache empty
            val settings = VaultSettings.getInstance(project)
            val token = settings.getToken()
            if (token != null) {
                 try {
                     runBlocking {
                         val client = VaultClientFactory.getClient(settings.vaultProvider)
                         val fetched = client.getSecrets(settings.vaultProject, settings.environment, token, settings.vaultUrl)
                         secrets = fetched.values.filter { it.length > 3 }
                     }
                 } catch (e: Exception) {
                     // Silently ignore if unable to fetch for pre-commit check
                 }
            }
        }

        if (secrets.isEmpty()) {
            return ReturnResult.COMMIT
        }

        val files = panel.virtualFiles
        val leakedSecrets = mutableListOf<String>()

        for (file in files) {
            if (file.isDirectory || file.extension == "jar" || file.extension == "class") continue
            
            try {
                val content = file.readText()
                for (secret in secrets) {
                    if (content.contains(secret)) {
                        leakedSecrets.add("File: \${file.name} contains a known vault secret!")
                    }
                }
            } catch (e: Exception) {
                // Skip files we can't read
            }
        }

        if (leakedSecrets.isNotEmpty()) {
            val message = """
                Security Warning: You are about to commit hardcoded secrets!
                
                ${leakedSecrets.joinToString("\n")}
                
                Please remove these secrets before committing.
            """.trimIndent()
            
            val result = Messages.showYesNoDialog(
                project,
                message,
                "Zero-Trust Secret Guardian",
                "Review Commit",
                "Commit Anyway (Unsafe)",
                Messages.getWarningIcon()
            )
            
            return if (result == Messages.YES) {
                ReturnResult.CANCEL
            } else {
                ReturnResult.COMMIT
            }
        }

        return ReturnResult.COMMIT
    }
}
