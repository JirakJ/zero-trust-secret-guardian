package com.jakubjirak.zerotrust.api

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class InfisicalClient : VaultClient {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            var request = chain.request()
            var response = chain.proceed(request)
            var tryCount = 0
            while (!response.isSuccessful && tryCount < 3) {
                tryCount++
                response.close()
                response = chain.proceed(request)
            }
            response
        }
        .build()

    private val gson = Gson()

    override suspend fun getSecrets(project: String, environment: String, token: String, customUrl: String?): Map<String, String> = withContext(Dispatchers.IO) {
        val baseUrl = if (customUrl.isNullOrBlank()) "https://app.infisical.com" else customUrl.trimEnd('/')
        val url = "\$baseUrl/api/v3/secrets/raw?workspaceId=\$project&environment=\$environment"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer \$token")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Infisical API error: \${response.code} \${response.message}")
            }
            
            val responseBody = response.body?.string() ?: return@withContext emptyMap()
            
            val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
            val result = mutableMapOf<String, String>()
            
            if (jsonObject != null && jsonObject.has("secrets")) {
                val secretsElement = jsonObject.get("secrets")
                if (secretsElement.isJsonObject) {
                    for ((key, valueElement) in secretsElement.asJsonObject.entrySet()) {
                        if (valueElement.isJsonPrimitive && valueElement.asJsonPrimitive.isString) {
                            result[key] = valueElement.asString
                        }
                    }
                } else if (secretsElement.isJsonArray) {
                    for (element in secretsElement.asJsonArray) {
                        if (element.isJsonObject) {
                            val obj = element.asJsonObject
                            val key = obj.get("secretKey")?.asString
                            val value = obj.get("secretValue")?.asString
                            if (key != null && value != null) {
                                result[key] = value
                            }
                        }
                    }
                }
            }
            return@withContext result
        }
    }
}
