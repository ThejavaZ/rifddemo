package dev.javiersg.rfiddemo.data.remote

import dev.javiersg.rfiddemo.data.remote.dto.AuthResponseDto
import dev.javiersg.rfiddemo.data.remote.dto.LoginRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class AuthApiClient(
    baseUrl: String = DEFAULT_BASE_URL,
    private val tokenStore: TokenStore,
) {
    private val httpClient =
        HttpClient(OkHttp) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
            }
            defaultRequest {
                url(baseUrl)
            }
        }

    suspend fun login(
        username: String,
        password: String,
    ): Result<Unit> =
        resultOf {
            val response =
                httpClient
                    .post(LOGIN_PATH) {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequestDto(username, password))
                    }.body<AuthResponseDto>()
            tokenStore.accessToken = response.token
        }

    fun logout() = tokenStore.clear()

    fun close() = httpClient.close()

    // runCatching capturaría CancellationException y rompería la propagación de cancelación.
    // Se captura Exception a propósito: el error se transporta como Result.
    @Suppress("TooGenericExceptionCaught")
    private suspend inline fun <T> resultOf(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    companion object {
        // Emulador Android -> host machine.
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
        const val LOGIN_PATH = "/api/v1/auth/token"
        private const val CONNECT_TIMEOUT_MS = 5_000L
        private const val REQUEST_TIMEOUT_MS = 10_000L
    }
}
