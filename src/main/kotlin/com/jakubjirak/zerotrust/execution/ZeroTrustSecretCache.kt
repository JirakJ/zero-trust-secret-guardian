package com.jakubjirak.zerotrust.execution

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class ZeroTrustSecretCache {
    private val currentSecrets = mutableMapOf<String, String>()

    fun setSecrets(secrets: Map<String, String>) {
        currentSecrets.clear()
        currentSecrets.putAll(secrets)
    }

    fun getSecrets(): Map<String, String> {
        return currentSecrets.toMap()
    }

    fun clear() {
        currentSecrets.clear()
    }

    companion object {
        fun getInstance(project: Project): ZeroTrustSecretCache {
            return project.getService(ZeroTrustSecretCache::class.java)
        }
    }
}
