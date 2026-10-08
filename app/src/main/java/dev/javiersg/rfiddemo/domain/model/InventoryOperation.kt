package dev.javiersg.rfiddemo.domain.model

import java.util.UUID

data class InventoryOperation(
    val id: String = UUID.randomUUID().toString(),
    val epc: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val locationId: String? = null
)
