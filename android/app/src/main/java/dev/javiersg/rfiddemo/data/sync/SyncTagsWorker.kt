package dev.javiersg.rfiddemo.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.javiersg.rfiddemo.diagnostics.DiagnosticLogger
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository

@HiltWorker
class SyncTagsWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted params: WorkerParameters,
        private val repository: LocalTagRepository,
        private val gateway: TagSyncGateway,
        private val logger: DiagnosticLogger,
    ) : CoroutineWorker(appContext, params) {
        override suspend fun doWork(): Result {
            val pendingTags = repository.getPendingSyncTags()
            if (pendingTags.isEmpty()) return Result.success()

            val epcs = pendingTags.map { it.epc }
            repository.markAsSyncing(epcs)

            return gateway.sendTags(pendingTags).fold(
                onSuccess = {
                    repository.markAsSynced(epcs)
                    logger.log("sync ok: ${epcs.size} tags")
                    Result.success()
                },
                onFailure = {
                    repository.markAsFailed(epcs)
                    logger.log("sync failed: ${epcs.size} tags (${it.message})")
                    val hasRetriesLeft = pendingTags.any { tag -> tag.retryCount + 1 < MAX_RETRIES }
                    if (hasRetriesLeft) Result.retry() else Result.success()
                },
            )
        }

        companion object {
            const val MAX_RETRIES = 5
        }
    }
