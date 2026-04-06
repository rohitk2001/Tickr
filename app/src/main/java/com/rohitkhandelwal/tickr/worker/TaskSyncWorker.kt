package com.rohitkhandelwal.tickr.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rohitkhandelwal.tickr.TickrApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaskSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val repository = (applicationContext as TickrApp).appContainer.taskRepository
        val result = repository.syncPendingTasks()

        if (result.failedCount > 0) {
            Result.retry()
        } else {
            Result.success()
        }
    }

    companion object {
        const val ONE_TIME_WORK_NAME = "task_sync_once"
        const val PERIODIC_WORK_NAME = "task_sync_periodic"
    }
}
