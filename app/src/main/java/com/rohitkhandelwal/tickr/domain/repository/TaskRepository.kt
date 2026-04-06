package com.rohitkhandelwal.tickr.domain.repository

import com.rohitkhandelwal.tickr.domain.model.Task
import com.rohitkhandelwal.tickr.domain.model.TaskFilter
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(filter: TaskFilter = TaskFilter.ALL): Flow<List<Task>>

    suspend fun getTask(taskId: String): Task?

    suspend fun createTask(
        title: String,
        description: String?,
        dueDate: Long?
    )

    suspend fun updateTask(
        taskId: String,
        title: String,
        description: String?,
        dueDate: Long?,
        isCompleted: Boolean
    )

    suspend fun toggleTaskCompleted(taskId: String, isCompleted: Boolean)

    suspend fun deleteTask(taskId: String)

    suspend fun syncPendingTasks(): SyncResult
}
