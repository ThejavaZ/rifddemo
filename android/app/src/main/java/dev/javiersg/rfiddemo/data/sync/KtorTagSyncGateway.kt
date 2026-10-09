package dev.javiersg.rfiddemo.data.sync

import dev.javiersg.rfiddemo.data.local.entity.TagEntity
import dev.javiersg.rfiddemo.data.remote.InventoryApiClient
import dev.javiersg.rfiddemo.data.remote.mapper.toDto

class KtorTagSyncGateway(
    private val apiClient: InventoryApiClient,
) : TagSyncGateway {
    override suspend fun sendTags(tags: List<TagEntity>): Result<Unit> =
        apiClient.pushTags(tags.map { it.toDto() }).map { }
}
