package com.jakubjirak.zerotrust.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VaultClientTest {

    @Test
    fun `factory returns the client for each provider, case-insensitively`() {
        assertTrue(VaultClientFactory.getClient("doppler") is DopplerClient)
        assertTrue(VaultClientFactory.getClient("Infisical") is InfisicalClient)
        assertTrue(VaultClientFactory.getClient("  DOPPLER ") is DopplerClient)
        VaultClientFactory.PROVIDERS.forEach { VaultClientFactory.getClient(it) }
    }

    @Test
    fun `factory rejects unknown providers`() {
        assertThrows(IllegalArgumentException::class.java) { VaultClientFactory.getClient("unknown") }
    }

    @Test
    fun `doppler request targets the download endpoint with encoded query and bearer token`() {
        val request = DopplerClient().buildRequest("my project", "dev&x", "tok-123", null)
        assertEquals("https", request.url.scheme)
        assertEquals("api.doppler.com", request.url.host)
        assertEquals("/v3/configs/config/secrets/download", request.url.encodedPath)
        assertEquals("json", request.url.queryParameter("format"))
        assertEquals("my project", request.url.queryParameter("project"))
        assertEquals("dev&x", request.url.queryParameter("config"))
        assertEquals("Bearer tok-123", request.header("Authorization"))
        assertEquals("application/json", request.header("Accept"))
    }

    @Test
    fun `custom base url replaces the default and trailing slashes are trimmed`() {
        val request = DopplerClient().buildRequest("p", "prod", "t", " https://vault.example.local:8443/ ")
        assertEquals("vault.example.local", request.url.host)
        assertEquals(8443, request.url.port)
        assertEquals("/v3/configs/config/secrets/download", request.url.encodedPath)
    }

    @Test
    fun `blank custom url falls back to the default`() {
        val request = InfisicalClient().buildRequest("ws", "dev", "t", "   ")
        assertEquals("app.infisical.com", request.url.host)
    }

    @Test
    fun `infisical request uses workspace and environment parameters`() {
        val request = InfisicalClient().buildRequest("ws-1", "staging", "tok", null)
        assertEquals("/api/v3/secrets/raw", request.url.encodedPath)
        assertEquals("ws-1", request.url.queryParameter("workspaceId"))
        assertEquals("staging", request.url.queryParameter("environment"))
        assertEquals("Bearer tok", request.header("Authorization"))
    }

    @Test
    fun `doppler parses flat string map and skips non-string values`() {
        val parsed = DopplerClient().parseSecrets("""{"API_KEY":"abc","PORT":8080,"NESTED":{"a":"b"},"EMPTY":""}""")
        assertEquals(mapOf("API_KEY" to "abc", "EMPTY" to ""), parsed)
    }

    @Test
    fun `doppler returns empty map for non-object json`() {
        assertTrue(DopplerClient().parseSecrets("[1,2]").isEmpty())
    }

    @Test
    fun `infisical parses array shape`() {
        val body = """{"secrets":[{"secretKey":"DB_URL","secretValue":"jdbc:x"},{"secretKey":"NOVALUE"},{"other":1},"str"]}"""
        assertEquals(mapOf("DB_URL" to "jdbc:x"), InfisicalClient().parseSecrets(body))
    }

    @Test
    fun `infisical parses object shape`() {
        val body = """{"secrets":{"TOKEN":"t-1","COUNT":3}}"""
        assertEquals(mapOf("TOKEN" to "t-1"), InfisicalClient().parseSecrets(body))
    }

    @Test
    fun `infisical returns empty map when secrets are missing or malformed`() {
        val client = InfisicalClient()
        assertTrue(client.parseSecrets("""{"imports":[]}""").isEmpty())
        assertTrue(client.parseSecrets("""{"secrets":"nope"}""").isEmpty())
        assertTrue(client.parseSecrets("\"just a string\"").isEmpty())
    }

    @Test
    fun `only server errors are retried`() {
        assertTrue(VaultHttp.shouldRetry(500))
        assertTrue(VaultHttp.shouldRetry(503))
        assertFalse(VaultHttp.shouldRetry(200))
        assertFalse(VaultHttp.shouldRetry(401))
        assertFalse(VaultHttp.shouldRetry(404))
    }
}
