package com.rohitkhandelwal.tickr.domain.model

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: TaskSyncStatus
)
