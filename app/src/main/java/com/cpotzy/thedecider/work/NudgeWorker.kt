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
 * Rules come from [NudgeSettings] so they can be tuned from the settings screen.
 */
class NudgeWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val prefs = NudgePrefs(ctx)
        val settings = NudgeSettings(ctx)
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val localNow = now.atZone(zone).toLocalTime()

        val windowStart = LocalTime.of(settings.windowStartHour, 0)
        val windowEnd = if (settings.windowEndHour >= 24) LocalTime.MAX else LocalTime.of(settings.windowEndHour, 0)
        if (localNow < windowStart || localNow >= windowEnd) return Result.success()
        if (prefs.nudgesFiredToday(zone) >= settings.maxPerDay) return Result.success()

        val minGap = Duration.ofMinutes(settings.minGapMinutes.toLong())
        prefs.lastNudgeAt?.let { last ->
            if (Duration.between(last, now) < minGap) return Result.success()
        }
        val quiet = Duration.ofMinutes(settings.quietAfterOpenMinutes.toLong())
        prefs.lastAppOpenAt?.let { lastOpen ->
            if (Duration.between(lastOpen, now) < quiet) return Result.success()
        }

        Notifications.showNudge(ctx)
        prefs.lastNudgeAt = now
        prefs.incrementNudgesFiredToday(zone)
        return Result.success()
    }

    companion object {
        private const val UNIQUE_NAME = "nudge-worker"

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
