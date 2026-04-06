package com.rohitkhandelwal.tickr.data.remote.dto

data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: Long?,
    val isCompleted: Boolean,
    val updatedAt: Long,
    val deleted: Boolean
)
