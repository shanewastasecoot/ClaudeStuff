package dev.heartmeter

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        refresh(applicationContext)
        Result.success()
    }

    companion object {
        private const val PERIODIC = "heartmeter-periodic"
        private const val ONE_SHOT = "heartmeter-now"

        /** Fetches usage, stores it, and redraws widgets. Safe to call off the main thread. */
        fun refresh(context: Context): String? {
            val prefs = Prefs(context)
            try {
                prefs.lastJson = UsageApi.fetch(prefs)
                prefs.lastSuccessAt = System.currentTimeMillis()
                prefs.lastError = null
            } catch (e: UsageApi.ApiException) {
                prefs.lastError = e.shortMessage
            } catch (e: Exception) {
                prefs.lastError = "ERROR"
            }
            UsageWidget.redrawAll(context)
            return prefs.lastError
        }

        private val online = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        fun schedulePeriodic(context: Context) {
            val req = PeriodicWorkRequestBuilder<UpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(online)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC, ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun refreshNow(context: Context) {
            val req = OneTimeWorkRequestBuilder<UpdateWorker>().setConstraints(online).build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_SHOT, ExistingWorkPolicy.REPLACE, req)
        }

        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC)
        }
    }
}
