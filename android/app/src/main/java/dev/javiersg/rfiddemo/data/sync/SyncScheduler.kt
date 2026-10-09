package dev.javiersg.rfiddemo.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class SyncScheduler(
    context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    // Encola la sincronización solo cuando haya red; KEEP evita duplicar un trabajo ya pendiente.
    fun scheduleSync() {
        val request =
            OneTimeWorkRequestBuilder<SyncTagsWorker>()
                .setConstraints(
                    Constraints
                        .Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                ).setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                .build()

        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    companion object {
        const val UNIQUE_WORK_NAME = "sync-tags"
        private const val BACKOFF_SECONDS = 30L
    }
}
