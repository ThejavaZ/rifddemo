package dev.javiersg.rfiddemo.data.sync

import dev.javiersg.rfiddemo.data.database.entity.TagEntity

interface TagSyncGateway {
    suspend fun sendTags(tags: List<TagEntity>): Result<Unit>
}
