package com.jakubjirak.zerotrust.api

import com.google.gson.JsonParser
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request

/** Doppler: `GET /v3/configs/config/secrets/download?format=json&project=…&config=…`. */
class DopplerClient : HttpVaultClient("Doppler") {

    override fun buildRequest(project: String, environment: String, token: String, customUrl: String?): Request {
        val url = "${baseUrl(customUrl, DEFAULT_URL)}/v3/configs/config/secrets/download".toHttpUrl()
            .newBuilder()
            .addQueryParameter("format", "json")
            .addQueryParameter("project", project)
            .addQueryParameter("config", environment)
            .build()
        return Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $token")
            .header("Accept", "application/json")
            .build()
    }

    /** The download endpoint returns a flat JSON object of `NAME: value`; non-string values are skipped. */
    override fun parseSecrets(body: String): Map<String, String> {
        val root = JsonParser.parseString(body)
        if (!root.isJsonObject) return emptyMap()
        return root.asJsonObject.entrySet()
            .filter { (_, value) -> value.isJsonPrimitive && value.asJsonPrimitive.isString }
            .associate { (key, value) -> key to value.asString }
    }

    companion object {
        const val DEFAULT_URL = "https://api.doppler.com"
    }
}
