package com.rohitkhandelwal.tickr.worker

interface SyncScheduler {
    fun enqueueSync()

    fun schedulePeriodicSync()
}
