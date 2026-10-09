package dev.javiersg.rfiddemo.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory",
    indices = [
        Index(value = ["epc"], unique = true),
    ],
)
data class InventoryEntity(
    @PrimaryKey
    val id: String,
    val epc: String,
    val name: String,
    val quantity: Int,
    val updatedAt: Long = System.currentTimeMillis(),
)
