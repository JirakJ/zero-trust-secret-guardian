package com.jakubjirak.zerotrust.util

import com.intellij.execution.ExecutionException
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.jakubjirak.zerotrust.api.VaultClientFactory
import com.jakubjirak.zerotrust.settings.VaultSettings
import kotlinx.coroutines.runBlocking

object SecretFetcher {

    @Throws(ExecutionException::class)
    fun fetchSecretsSynchronously(project: Project): Map<String, String> {
        val settings = VaultSettings.getInstance(project)

        val provider = settings.vaultProvider
        val env = settings.environment
        val token = settings.getToken()
        val vaultProject = settings.vaultProject
        val vaultUrl = settings.vaultUrl

        if (token.isNullOrBlank()) {
            return emptyMap()
        }

        var secretsFetched = emptyMap<String, String>()
        var proceed = true

        val task = object : Task.Modal(project, "Fetching Zero-Trust Secrets", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Connecting to $provider..."
                
                var retry = true
                while (retry) {
                    retry = false
                    try {
                        secretsFetched = runBlocking {
                            val client = VaultClientFactory.getClient(provider)
                            client.getSecrets(vaultProject, env, token, vaultUrl)
                        }
                    } catch (e: Exception) {
                        if (indicator.isCanceled) {
                            proceed = false
                            return
                        }
                        
                        ApplicationManager.getApplication().invokeAndWait {
                            val result = Messages.showYesNoCancelDialog(
                                project,
                                "Failed to fetch secrets from $provider: ${e.message}\n\n" +
                                    "Do you want to retry, continue without secrets, or cancel execution?",
                                "Zero-Trust Vault Error",
                                "Retry",
                                "Continue Without Secrets",
                                "Cancel Execution",
                                Messages.getErrorIcon()
                            )
                            
                            when (result) {
                                Messages.YES -> retry = true
                                Messages.NO -> {
                                    proceed = true
                                    secretsFetched = emptyMap()
                                }
                                else -> proceed = false
                            }
                        }
                    }
                }
            }
        }

        ProgressManager.getInstance().run(task)

        if (!proceed) {
            throw ExecutionException("Execution cancelled by user during Zero-Trust secret fetch.")
        }

        return secretsFetched
    }
}
