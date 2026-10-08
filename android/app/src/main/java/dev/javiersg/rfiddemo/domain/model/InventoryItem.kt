package dev.javiersg.rfiddemo.domain.model

data class InventoryItem(
    val id: String,
    val epc: String,
    val name: String,
    val quantity: Int
)
