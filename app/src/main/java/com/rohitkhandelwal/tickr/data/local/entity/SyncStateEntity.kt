package com.rohitkhandelwal.tickr.data.local.entity

enum class SyncStateEntity {
    SYNCED,
    PENDING_CREATE,
    PENDING_UPDATE,
    PENDING_DELETE,
    SYNCING,
    FAILED_CREATE,
    FAILED_UPDATE,
    FAILED_DELETE
}
