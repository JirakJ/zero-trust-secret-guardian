package com.jakubjirak.zerotrust.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface VaultClient {
    suspend fun getSecrets(project: String, environment: String, token: String, customUrl: String? = null): Map<String, String>
}

object VaultClientFactory {
    fun getClient(provider: String): VaultClient {
        return when (provider.lowercase()) {
            "doppler" -> DopplerClient()
            "infisical" -> InfisicalClient()
            else -> throw IllegalArgumentException("Unknown vault provider: $provider")
        }
    }
}
