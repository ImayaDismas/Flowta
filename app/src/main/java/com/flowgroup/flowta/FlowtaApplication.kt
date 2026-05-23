package com.flowgroup.flowta

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.flowgroup.flowta.reminder.DeniNotifier
import com.flowgroup.flowta.reminder.DeniReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class FlowtaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        System.loadLibrary("sqlcipher")
        DeniNotifier.ensureChannel(this)
        scheduleDeniReminder()
    }

    private fun scheduleDeniReminder() {
        val request = PeriodicWorkRequestBuilder<DeniReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            DeniReminderWorker.UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
