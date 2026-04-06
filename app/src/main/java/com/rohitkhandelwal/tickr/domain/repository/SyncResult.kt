package com.rohitkhandelwal.tickr.domain.repository

data class SyncResult(
    val syncedCount: Int,
    val failedCount: Int
)
