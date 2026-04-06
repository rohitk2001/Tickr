package com.rohitkhandelwal.tickr.data.mapper

import com.rohitkhandelwal.tickr.data.local.entity.SyncStateEntity
import com.rohitkhandelwal.tickr.data.local.entity.TaskEntity
import com.rohitkhandelwal.tickr.data.remote.dto.TaskDto
import com.rohitkhandelwal.tickr.domain.model.Task
import com.rohitkhandelwal.tickr.domain.model.TaskSyncStatus

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncState.toDomainSyncStatus()
    )
}

fun SyncStateEntity.toDomainSyncStatus(): TaskSyncStatus {
    return when (this) {
        SyncStateEntity.SYNCED -> TaskSyncStatus.SYNCED
        SyncStateEntity.SYNCING -> TaskSyncStatus.SYNCING
        SyncStateEntity.FAILED_CREATE,
        SyncStateEntity.FAILED_UPDATE,
        SyncStateEntity.FAILED_DELETE -> TaskSyncStatus.FAILED
        SyncStateEntity.PENDING_CREATE,
        SyncStateEntity.PENDING_UPDATE,
        SyncStateEntity.PENDING_DELETE -> TaskSyncStatus.PENDING
    }
}

fun TaskEntity.toTaskDto(): TaskDto {
    return TaskDto(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        isCompleted = isCompleted,
        updatedAt = updatedAt,
        deleted = isDeleted
    )
}
