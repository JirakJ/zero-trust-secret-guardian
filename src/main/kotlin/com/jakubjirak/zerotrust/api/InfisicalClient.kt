package com.jakubjirak.zerotrust.api

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

/** Infisical: `GET /api/v3/secrets/raw?workspaceId=…&environment=…`. */
class InfisicalClient : HttpVaultClient("Infisical") {

    override fun buildRequest(project: String, environment: String, token: String, customUrl: String?): Request {
        val url = "${baseUrl(customUrl, DEFAULT_URL)}/api/v3/secrets/raw".toHttpUrl()
            .newBuilder()
            .addQueryParameter("workspaceId", project)
            .addQueryParameter("environment", environment)
            .build()
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()
    }

    /**
     * Accepts both shapes Infisical has used for `secrets`: an array of
     * `{secretKey, secretValue}` objects, or a flat `{NAME: value}` object.
     */
    override fun parseSecrets(body: String): Map<String, String> {
        val root = JsonParser.parseString(body)
        if (!root.isJsonObject) return emptyMap()
        val secrets = root.asJsonObject.get("secrets") ?: return emptyMap()
        return when {
            secrets.isJsonObject -> secrets.asJsonObject.entrySet()
                .filter { (_, value) -> value.isString() }
                .associate { (key, value) -> key to value.asString }
            secrets.isJsonArray -> secrets.asJsonArray
                .filter { it.isJsonObject }
                .mapNotNull { element ->
                    val obj = element.asJsonObject
                    val key = obj.get("secretKey")?.takeIf { it.isString() }?.asString
                    val value = obj.get("secretValue")?.takeIf { it.isString() }?.asString
                    if (key != null && value != null) key to value else null
                }
                .toMap()
            else -> emptyMap()
        }
    }

    private fun JsonElement.isString(): Boolean = isJsonPrimitive && asJsonPrimitive.isString

    companion object {
        const val DEFAULT_URL = "https://app.infisical.com"
    }
}
