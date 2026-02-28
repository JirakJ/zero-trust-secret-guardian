package com.jakubjirak.zerotrust.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

class DopplerClient : VaultClient {
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
        val baseUrl = if (customUrl.isNullOrBlank()) "https://api.doppler.com" else customUrl.trimEnd('/')
        val url = "\$baseUrl/v3/configs/config/secrets/download?format=json&project=\$project&config=\$environment"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer \$token")
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Doppler API error: \${response.code} \${response.message}")
            }
            
            val responseBody = response.body?.string() ?: return@withContext emptyMap()
            
            val mapType = object : TypeToken<Map<String, String>>() {}.type
            return@withContext gson.fromJson(responseBody, mapType) ?: emptyMap()
        }
    }
}
