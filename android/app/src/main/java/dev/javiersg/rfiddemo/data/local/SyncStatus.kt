package dev.javiersg.rfiddemo.data.local

enum class SyncStatus {
    PENDING,   // Guardado localmente, pendiente de subida
    SYNCED,    // Confirmado por el servidor
    FAILED     // Reintentos agotados / error de red
}