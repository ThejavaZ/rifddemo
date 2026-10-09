package dev.javiersg.rfiddemo.data.sync

import dev.javiersg.rfiddemo.data.local.entity.TagEntity

interface TagSyncGateway {
    suspend fun sendTags(tags: List<TagEntity>): Result<Unit>
}
