package com.jakubjirak.zerotrust.execution

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ZeroTrustSecretCacheTest {

    @Test
    fun `test cache stores and retrieves secrets`() {
        val cache = ZeroTrustSecretCache()
        val secrets = mapOf("KEY1" to "VALUE1", "KEY2" to "VALUE2")
        
        cache.setSecrets(secrets)
        
        val retrieved = cache.getSecrets()
        assertEquals(2, retrieved.size)
        assertEquals("VALUE1", retrieved["KEY1"])
    }

    @Test
    fun `test cache clear`() {
        val cache = ZeroTrustSecretCache()
        cache.setSecrets(mapOf("KEY1" to "VALUE1"))
        
        cache.clear()
        
        assertTrue(cache.getSecrets().isEmpty())
    }
}
