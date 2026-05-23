package com.flowgroup.flowta.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flowgroup.flowta.domain.repository.DeniRepository
import com.flowgroup.flowta.domain.repository.PreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import com.flowgroup.flowta.domain.common.Result as DomainResult

/**
 * Periodic reminder that posts a local notification when clients still owe money (deni).
 * Uses a Hilt [EntryPoint] rather than @HiltWorker so it works with WorkManager's default factory.
 */
class DeniReminderWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun deniRepository(): DeniRepository
        fun preferencesRepository(): PreferencesRepository
    }

    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        val businessId = deps.preferencesRepository().currentBusinessId.firstOrNull()
            ?: return Result.success()
        val totalResult = deps.deniRepository().observeTotalOutstanding(businessId).first()
        val total = (totalResult as? DomainResult.Success)?.data ?: 0L
        if (total > 0L) {
            DeniNotifier.show(applicationContext)
        }
        return Result.success()
    }

    companion object { const val UNIQUE_NAME = "deni_reminder" }
}
