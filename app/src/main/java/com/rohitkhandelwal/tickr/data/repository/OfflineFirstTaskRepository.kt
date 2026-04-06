package com.rohitkhandelwal.tickr.data.repository

import com.rohitkhandelwal.tickr.core.time.TimeProvider
import com.rohitkhandelwal.tickr.data.local.dao.TaskDao
import com.rohitkhandelwal.tickr.data.local.entity.SyncStateEntity
import com.rohitkhandelwal.tickr.data.local.entity.TaskEntity
import com.rohitkhandelwal.tickr.data.mapper.toDomain
import com.rohitkhandelwal.tickr.data.mapper.toTaskDto
import com.rohitkhandelwal.tickr.data.remote.datasource.TaskRemoteDataSource
import com.rohitkhandelwal.tickr.domain.model.Task
import com.rohitkhandelwal.tickr.domain.model.TaskFilter
import com.rohitkhandelwal.tickr.domain.repository.SyncResult
import com.rohitkhandelwal.tickr.domain.repository.TaskRepository
import com.rohitkhandelwal.tickr.worker.SyncScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class OfflineFirstTaskRepository(
    private val taskDao: TaskDao,
    private val remoteDataSource: TaskRemoteDataSource,
    private val timeProvider: TimeProvider,
    private val syncScheduler: SyncScheduler
) : TaskRepository {

    override fun observeTasks(filter: TaskFilter): Flow<List<Task>> {
        val source = when (filter) {
            TaskFilter.ALL -> taskDao.observeAllTasks()
            TaskFilter.ACTIVE -> taskDao.observeActiveTasks()
            TaskFilter.COMPLETED -> taskDao.observeCompletedTasks()
        }

        return source.map { taskEntities ->
            taskEntities.map(TaskEntity::toDomain)
        }
    }

    override suspend fun getTask(taskId: String): Task? {
        return taskDao.getTaskById(taskId)?.toDomain()
    }

    override suspend fun createTask(
        title: String,
        description: String?,
        dueDate: Long?
    ) {
        val now = timeProvider.now()

        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            dueDate = dueDate,
            isCompleted = false,
            isDeleted = false,
            createdAt = now,
            updatedAt = now,
            syncState = SyncStateEntity.PENDING_CREATE,
            syncErrorMessage = null
        )

        taskDao.insertTask(task)
        syncScheduler.enqueueSync()
    }

    override suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueDate: Long?,
        isCompleted: Boolean
    ) {
        val existingTask = taskDao.getTaskById(taskId) ?: return

        val updatedTask = existingTask.copy(
            title = title,
            description = description,
            dueDate = dueDate,
            isCompleted = isCompleted,
            updatedAt = timeProvider.now(),
            syncState = updatedSyncState(existingTask.syncState),
            syncErrorMessage = null
        )

        taskDao.updateTask(updatedTask)
        syncScheduler.enqueueSync()
    }

    override suspend fun toggleTaskCompleted(taskId: String, isCompleted: Boolean) {
        val existingTask = taskDao.getTaskById(taskId) ?: return

        val updatedTask = existingTask.copy(
            isCompleted = isCompleted,
            updatedAt = timeProvider.now(),
            syncState = updatedSyncState(existingTask.syncState),
            syncErrorMessage = null
        )

        taskDao.updateTask(updatedTask)
        syncScheduler.enqueueSync()
    }

    override suspend fun deleteTask(taskId: String) {
        val existingTask = taskDao.getTaskById(taskId) ?: return

        if (existingTask.syncState == SyncStateEntity.PENDING_CREATE ||
            existingTask.syncState == SyncStateEntity.FAILED_CREATE
        ) {
            taskDao.deleteTaskById(taskId)
            return
        }

        val deletedTask = existingTask.copy(
            isDeleted = true,
            updatedAt = timeProvider.now(),
            syncState = SyncStateEntity.PENDING_DELETE,
            syncErrorMessage = null
        )

        taskDao.updateTask(deletedTask)
        syncScheduler.enqueueSync()
    }

    override suspend fun syncPendingTasks(): SyncResult {
        val pendingTasks = taskDao.getPendingSyncTasks()
        var syncedCount = 0
        var failedCount = 0

        pendingTasks.forEach { task ->
            try {
                markSyncing(task)

                when (task.syncState) {
                    SyncStateEntity.PENDING_CREATE,
                    SyncStateEntity.FAILED_CREATE,
                    SyncStateEntity.PENDING_UPDATE,
                    SyncStateEntity.FAILED_UPDATE -> syncUpsert(task)

                    SyncStateEntity.PENDING_DELETE,
                    SyncStateEntity.FAILED_DELETE -> syncDelete(task)

                    SyncStateEntity.SYNCED,
                    SyncStateEntity.SYNCING -> Unit
                }

                syncedCount++
            } catch (exception: Exception) {
                failedCount++
                markFailure(task, exception)
            }
        }

        return SyncResult(
            syncedCount = syncedCount,
            failedCount = failedCount
        )
    }

    private fun updatedSyncState(currentState: SyncStateEntity): SyncStateEntity {
        return when (currentState) {
            SyncStateEntity.PENDING_CREATE -> SyncStateEntity.PENDING_CREATE
            SyncStateEntity.PENDING_DELETE -> SyncStateEntity.PENDING_DELETE
            SyncStateEntity.FAILED_CREATE -> SyncStateEntity.PENDING_CREATE
            SyncStateEntity.FAILED_DELETE -> SyncStateEntity.PENDING_DELETE
            SyncStateEntity.SYNCED,
            SyncStateEntity.SYNCING,
            SyncStateEntity.FAILED_UPDATE,
            SyncStateEntity.PENDING_UPDATE -> SyncStateEntity.PENDING_UPDATE
        }
    }

    private suspend fun markSyncing(task: TaskEntity) {
        taskDao.updateTask(
            task.copy(
                syncState = SyncStateEntity.SYNCING,
                syncErrorMessage = null
            )
        )
    }

    private suspend fun syncUpsert(task: TaskEntity) {
        remoteDataSource.upsertTask(task.toTaskDto())
        taskDao.updateTask(
            task.copy(
                syncState = SyncStateEntity.SYNCED,
                syncErrorMessage = null
            )
        )
    }

    private suspend fun syncDelete(task: TaskEntity) {
        remoteDataSource.deleteTask(task.id)
        taskDao.deleteTaskById(task.id)
    }

    private suspend fun markFailure(task: TaskEntity, exception: Exception) {
        taskDao.updateTask(
            task.copy(
                syncState = task.syncState.toFailureState(),
                syncErrorMessage = exception.message
            )
        )
    }

    private fun SyncStateEntity.toFailureState(): SyncStateEntity {
        return when (this) {
            SyncStateEntity.PENDING_CREATE,
            SyncStateEntity.FAILED_CREATE -> SyncStateEntity.FAILED_CREATE

            SyncStateEntity.PENDING_DELETE,
            SyncStateEntity.FAILED_DELETE -> SyncStateEntity.FAILED_DELETE

            SyncStateEntity.PENDING_UPDATE,
            SyncStateEntity.FAILED_UPDATE,
            SyncStateEntity.SYNCED,
            SyncStateEntity.SYNCING -> SyncStateEntity.FAILED_UPDATE
        }
    }
}
