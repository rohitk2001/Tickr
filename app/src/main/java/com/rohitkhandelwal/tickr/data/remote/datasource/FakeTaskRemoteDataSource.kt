package com.rohitkhandelwal.tickr.data.remote.datasource

import com.rohitkhandelwal.tickr.data.remote.dto.TaskDto
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

class FakeTaskRemoteDataSource : TaskRemoteDataSource {

    private val remoteTasks = ConcurrentHashMap<String, TaskDto>()

    override suspend fun upsertTask(task: TaskDto) {
        delay(NETWORK_DELAY_MS)
        maybeFail(task.title, task.description)
        remoteTasks[task.id] = task
    }

    override suspend fun deleteTask(taskId: String) {
        delay(NETWORK_DELAY_MS)
        remoteTasks.remove(taskId)
    }

    private fun maybeFail(title: String, description: String?) {
        val failureMarker = "[fail-sync]"
        if (title.contains(failureMarker, ignoreCase = true) ||
            description.orEmpty().contains(failureMarker, ignoreCase = true)
        ) {
            throw IOException("Simulated sync failure. Remove [fail-sync] to allow retry.")
        }
    }

    private companion object {
        const val NETWORK_DELAY_MS = 750L
    }
}
