package com.jakubjirak.zerotrust.execution

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

/**
 * In-memory copy of the secrets injected into the last run, used for console masking
 * and the pre-commit scan. Read from console reader threads and written from the run
 * pipeline, so the map is swapped atomically rather than mutated in place.
 */
@Service(Service.Level.PROJECT)
class ZeroTrustSecretCache {
    @Volatile
    private var currentSecrets: Map<String, String> = emptyMap()

    fun setSecrets(secrets: Map<String, String>) {
        currentSecrets = secrets.toMap()
    }

    fun getSecrets(): Map<String, String> = currentSecrets

    fun clear() {
        currentSecrets = emptyMap()
    }

    companion object {
        fun getInstance(project: Project): ZeroTrustSecretCache {
            return project.getService(ZeroTrustSecretCache::class.java)
        }
    }
}
