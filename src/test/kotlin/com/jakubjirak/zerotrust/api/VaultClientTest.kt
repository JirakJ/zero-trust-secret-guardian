package com.jakubjirak.zerotrust.api

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class VaultClientTest {

    @Test
    fun `test client factory returns correct client`() {
        val doppler = VaultClientFactory.getClient("doppler")
        assertTrue(doppler is DopplerClient)

        val infisical = VaultClientFactory.getClient("infisical")
        assertTrue(infisical is InfisicalClient)

        assertThrows(IllegalArgumentException::class.java) {
            VaultClientFactory.getClient("unknown")
        }
    }
}
