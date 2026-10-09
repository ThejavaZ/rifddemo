package dev.javiersg.rfiddemo.data.api

import dev.javiersg.rfiddemo.data.api.dto.InventoryResponseDto
import dev.javiersg.rfiddemo.data.api.dto.SyncTagsRequestDto
import dev.javiersg.rfiddemo.data.api.dto.SyncTagsResponseDto
import dev.javiersg.rfiddemo.data.api.dto.TagDto
import dev.javiersg.rfiddemo.data.api.mapper.toDomain
import dev.javiersg.rfiddemo.domain.model.InventoryItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json

class InventoryApiClient(
    baseUrl: String = DEFAULT_BASE_URL,
    private val tokenStore: TokenStore? = null,
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

    suspend fun pushTags(tags: List<TagDto>): Result<SyncTagsResponseDto> =
        resultOf {
            httpClient
                .post(SYNC_PATH) {
                    attachToken()
                    contentType(ContentType.Application.Json)
                    setBody(SyncTagsRequestDto(tags))
                }.body<SyncTagsResponseDto>()
        }

    suspend fun getInventory(epc: String): Result<InventoryItem> =
        resultOf {
            httpClient
                .get("$INVENTORY_PATH/$epc") {
                    attachToken()
                }.body<InventoryResponseDto>()
                .toDomain()
        }

    private fun io.ktor.client.request.HttpRequestBuilder.attachToken() {
        tokenStore?.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") }
    }

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
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8080"
        const val SYNC_PATH = "/api/v1/sync/tags"
        const val INVENTORY_PATH = "/api/v1/inventory"
        private const val CONNECT_TIMEOUT_MS = 5_000L
        private const val REQUEST_TIMEOUT_MS = 10_000L
    }
}
