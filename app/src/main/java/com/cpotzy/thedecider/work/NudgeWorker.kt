package com.cpotzy.thedecider.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that decides whether to fire a nudge notification.
 *
 * Rules (hardcoded for v1):
 *  - Only inside the nudge window (09:00–21:00 local)
 *  - At most 3 nudges per day
 *  - At least 60 minutes between nudges
 *  - Skip if the app was opened in the last 2 hours
 */
class NudgeWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val prefs = NudgePrefs(ctx)
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val localNow = now.atZone(zone).toLocalTime()

        if (localNow < WINDOW_START || localNow >= WINDOW_END) return Result.success()
        if (prefs.nudgesFiredToday(zone) >= MAX_PER_DAY) return Result.success()

        prefs.lastNudgeAt?.let { last ->
            if (Duration.between(last, now) < MIN_GAP_BETWEEN_NUDGES) return Result.success()
        }
        prefs.lastAppOpenAt?.let { lastOpen ->
            if (Duration.between(lastOpen, now) < QUIET_AFTER_OPEN) return Result.success()
        }

        Notifications.showNudge(ctx)
        prefs.lastNudgeAt = now
        prefs.incrementNudgesFiredToday(zone)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "nudge-worker"
        private val WINDOW_START = LocalTime.of(9, 0)
        private val WINDOW_END = LocalTime.of(21, 0)
        private const val MAX_PER_DAY = 3
        private val MIN_GAP_BETWEEN_NUDGES = Duration.ofMinutes(60)
        private val QUIET_AFTER_OPEN = Duration.ofHours(2)

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<NudgeWorker>(
                repeatInterval = 30, repeatIntervalTimeUnit = TimeUnit.MINUTES,
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request,
            )
        }
    }
}
