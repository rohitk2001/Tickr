package com.rohitkhandelwal.tickr.data.remote.datasource

import com.rohitkhandelwal.tickr.data.remote.dto.TaskDto

interface TaskRemoteDataSource {
    suspend fun upsertTask(task: TaskDto)

    suspend fun deleteTask(taskId: String)
}
