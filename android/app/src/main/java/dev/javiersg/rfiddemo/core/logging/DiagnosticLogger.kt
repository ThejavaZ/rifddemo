package dev.javiersg.rfiddemo.core.logging

import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Registro local de eventos (conexiones, lecturas, sync, login).
 * Buffer circular en memoria: se exporta desde Ajustes vía share-sheet.
 */
@Singleton
class DiagnosticLogger
    @Inject
    constructor() {
        private val lock = Any()
        private val entries = ArrayDeque<String>()

        fun log(event: String) {
            synchronized(lock) {
                if (entries.size >= MAX_ENTRIES) entries.removeFirst()
                entries.addLast("${Instant.now()} $event")
            }
        }

        fun dump(): String = synchronized(lock) { entries.joinToString(separator = "\n") }

        fun clear() = synchronized(lock) { entries.clear() }

        private companion object {
            const val MAX_ENTRIES = 200
        }
    }
