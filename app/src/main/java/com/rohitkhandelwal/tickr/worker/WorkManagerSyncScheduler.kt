package com.rohitkhandelwal.tickr.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class WorkManagerSyncScheduler(
    context: Context
) : SyncScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<TaskSyncWorker>()
            .setConstraints(syncConstraints())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10,
                TimeUnit.SECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            TaskSyncWorker.ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun schedulePeriodicSync() {
        val request = PeriodicWorkRequestBuilder<TaskSyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(syncConstraints())
            .build()

        workManager.enqueueUniquePeriodicWork(
            TaskSyncWorker.PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun syncConstraints(): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
