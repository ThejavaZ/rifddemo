package dev.javiersg.rfiddemo.data.sync

import dev.javiersg.rfiddemo.data.api.InventoryApiClient
import dev.javiersg.rfiddemo.data.api.mapper.toDto
import dev.javiersg.rfiddemo.data.database.entity.TagEntity

class KtorTagSyncGateway(
    private val apiClient: InventoryApiClient,
) : TagSyncGateway {
    override suspend fun sendTags(tags: List<TagEntity>): Result<Unit> =
        apiClient.pushTags(tags.map { it.toDto() }).map { }
}
