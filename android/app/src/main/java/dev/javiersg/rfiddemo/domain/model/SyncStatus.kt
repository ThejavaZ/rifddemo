package dev.javiersg.rfiddemo.domain.model

enum class SyncStatus {
    PENDING,  // Capturado localmente en Room, esperando conexión/WorkManager
    SYNCING,  // WorkManager intentando enviar a ASP.NET API
    SYNCED,   // Confirmado exitosamente por el servidor
    FAILED    // Error persistente (p. ej. validación, conflicto de datos)
}