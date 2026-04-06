package com.rohitkhandelwal.tickr.di

import android.content.Context
import androidx.room.Room
import com.rohitkhandelwal.tickr.core.time.SystemTimeProvider
import com.rohitkhandelwal.tickr.data.local.db.TickrDatabase
import com.rohitkhandelwal.tickr.data.remote.datasource.FakeTaskRemoteDataSource
import com.rohitkhandelwal.tickr.data.remote.datasource.TaskRemoteDataSource
import com.rohitkhandelwal.tickr.data.repository.OfflineFirstTaskRepository
import com.rohitkhandelwal.tickr.domain.repository.TaskRepository
import com.rohitkhandelwal.tickr.worker.SyncScheduler
import com.rohitkhandelwal.tickr.worker.WorkManagerSyncScheduler

interface AppContainer {
    val taskRepository: TaskRepository
    val syncScheduler: SyncScheduler
}

class DefaultAppContainer(
    context: Context
) : AppContainer {

    private val appContext = context.applicationContext

    private val database: TickrDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            TickrDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    private val remoteDataSource: TaskRemoteDataSource by lazy {
        FakeTaskRemoteDataSource()
    }

    override val syncScheduler: SyncScheduler by lazy {
        WorkManagerSyncScheduler(appContext)
    }

    override val taskRepository: TaskRepository by lazy {
        OfflineFirstTaskRepository(
            taskDao = database.taskDao(),
            remoteDataSource = remoteDataSource,
            timeProvider = SystemTimeProvider,
            syncScheduler = syncScheduler
        )
    }

    private companion object {
        const val DATABASE_NAME = "tickr.db"
    }
}
