package com.rohitkhandelwal.tickr.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val isCompleted: Boolean,
    val isDeleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val syncState: SyncStateEntity,
    val syncErrorMessage: String?
)