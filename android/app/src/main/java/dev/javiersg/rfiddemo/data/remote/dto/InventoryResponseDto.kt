package dev.javiersg.rfiddemo.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InventoryResponseDto(
    val id: String,
    val epc: String,
    val name: String,
    val quantity: Int
)
