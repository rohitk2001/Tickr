package com.rohitkhandelwal.tickr

import android.app.Application
import com.rohitkhandelwal.tickr.di.AppContainer
import com.rohitkhandelwal.tickr.di.DefaultAppContainer

class TickrApp : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = DefaultAppContainer(this)
        appContainer.syncScheduler.enqueueSync()
        appContainer.syncScheduler.schedulePeriodicSync()
    }
}
