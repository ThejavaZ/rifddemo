package dev.javiersg.rfiddemo.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.javiersg.rfiddemo.RfidApplication
import dev.javiersg.rfiddemo.domain.repository.LocalTagRepository

class SyncTagsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as RfidApplication).container
        val repository = container.localTagRepository
        val gateway = container.tagSyncGateway

        val pendingTags = repository.getPendingSyncTags()
        if (pendingTags.isEmpty()) return Result.success()

        val epcs = pendingTags.map { it.epc }
        repository.markAsSyncing(epcs)

        return gateway.sendTags(pendingTags).fold(
            onSuccess = {
                repository.markAsSynced(epcs)
                Result.success()
            },
            onFailure = {
                repository.markAsFailed(epcs)
                val hasRetriesLeft = pendingTags.any { it.retryCount + 1 < MAX_RETRIES }
                if (hasRetriesLeft) Result.retry() else Result.success()
            }
        )
    }

    companion object {
        const val MAX_RETRIES = 5
    }
}
