package com.jakubjirak.zerotrust.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/** Downloads the secrets of one project/environment from a cloud vault. */
interface VaultClient {
    suspend fun getSecrets(project: String, environment: String, token: String, customUrl: String? = null): Map<String, String>
}

object VaultClientFactory {
    /** Supported provider names as shown in the UI. */
    val PROVIDERS = listOf("Infisical", "Doppler")

    fun getClient(provider: String): VaultClient {
        return when (provider.trim().lowercase()) {
            "doppler" -> DopplerClient()
            "infisical" -> InfisicalClient()
            else -> throw IllegalArgumentException("Unknown vault provider: $provider")
        }
    }
}

/**
 * Base for REST vault clients: builds a request ([buildRequest]), executes it off the EDT
 * and turns the body into a key/value map ([parseSecrets]). Request building and parsing
 * are pure so they can be unit-tested without network access.
 */
abstract class HttpVaultClient(
    private val providerName: String,
    private val http: OkHttpClient = VaultHttp.client,
) : VaultClient {

    abstract fun buildRequest(project: String, environment: String, token: String, customUrl: String?): Request

    abstract fun parseSecrets(body: String): Map<String, String>

    override suspend fun getSecrets(
        project: String,
        environment: String,
        token: String,
        customUrl: String?,
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val request = buildRequest(project, environment, token, customUrl)
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("$providerName API error: ${response.code} ${response.message}")
            }
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) emptyMap() else parseSecrets(body)
        }
    }

    protected fun baseUrl(customUrl: String?, default: String): String =
        if (customUrl.isNullOrBlank()) default else customUrl.trim().trimEnd('/')
}

/** Shared HTTP client: sane timeouts and a bounded retry for transient (5xx / I/O) failures only. */
object VaultHttp {
    const val MAX_RETRIES = 2
    private const val TIMEOUT_SECONDS = 20L

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(RetryOnServerError(MAX_RETRIES))
            .build()
    }

    /** Retries server errors (5xx) up to [maxRetries] times; client errors such as 401/403/404 are returned immediately. */
    class RetryOnServerError(private val maxRetries: Int) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response = chain.proceed(request)
            var attempt = 0
            while (shouldRetry(response.code) && attempt < maxRetries) {
                attempt++
                response.close()
                response = chain.proceed(request)
            }
            return response
        }
    }

    fun shouldRetry(code: Int): Boolean = code in 500..599
}
