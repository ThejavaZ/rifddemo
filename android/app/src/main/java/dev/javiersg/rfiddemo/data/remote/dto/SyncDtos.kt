package dev.javiersg.rfiddemo.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val epc: String,
    val rssi: Int,
    val antenna: Int,
    val readCount: Int,
    val lastSeenTimestamp: Long,
)

@Serializable
data class SyncTagsRequestDto(
    val tags: List<TagDto>,
)

@Serializable
data class SyncTagsResponseDto(
    val syncedCount: Int,
)
