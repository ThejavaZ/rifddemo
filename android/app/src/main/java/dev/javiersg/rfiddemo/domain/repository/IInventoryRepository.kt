package dev.javiersg.rfiddemo.domain.repository

import dev.javiersg.rfiddemo.domain.model.InventoryOperation
import dev.javiersg.rfiddemo.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface IInventoryRepository {
    /** Retorna todas las operaciones registradas localmente en tiempo real */
    fun getAllOperations(): Flow<List<InventoryOperation>>

    /** Retorna operaciones filtradas por su estado en la cola de sincronización */
    fun getOperationsByStatus(status: SyncStatus): Flow<List<InventoryOperation>>

    /** Guarda una nueva lectura en la base de datos local */
    suspend fun saveOperation(operation: InventoryOperation)

    /** Actualiza el estado de sincronización (PENDING -> SYNCING -> SYNCED) */
    suspend fun updateSyncStatus(
        id: String,
        status: SyncStatus,
    )
}
