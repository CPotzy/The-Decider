package com.cpotzy.thedecider.work

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NudgePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var lastNudgeAt: Instant?
        get() = prefs.getLong(KEY_LAST_NUDGE_AT, -1L).takeIf { it > 0 }?.let(Instant::ofEpochMilli)
        set(value) { prefs.edit().putLong(KEY_LAST_NUDGE_AT, value?.toEpochMilli() ?: -1L).apply() }

    var lastAppOpenAt: Instant?
        get() = prefs.getLong(KEY_LAST_APP_OPEN_AT, -1L).takeIf { it > 0 }?.let(Instant::ofEpochMilli)
        set(value) { prefs.edit().putLong(KEY_LAST_APP_OPEN_AT, value?.toEpochMilli() ?: -1L).apply() }

    /** How many nudges have fired today (caller resets when day rolls over). */
    fun nudgesFiredToday(zone: ZoneId = ZoneId.systemDefault()): Int {
        val storedDay = prefs.getString(KEY_NUDGE_DAY, null)
        val today = LocalDate.now(zone).toString()
        if (storedDay != today) return 0
        return prefs.getInt(KEY_NUDGE_COUNT_TODAY, 0)
    }

    fun incrementNudgesFiredToday(zone: ZoneId = ZoneId.systemDefault()) {
        val today = LocalDate.now(zone).toString()
        val storedDay = prefs.getString(KEY_NUDGE_DAY, null)
        val count = if (storedDay == today) prefs.getInt(KEY_NUDGE_COUNT_TODAY, 0) + 1 else 1
        prefs.edit()
            .putString(KEY_NUDGE_DAY, today)
            .putInt(KEY_NUDGE_COUNT_TODAY, count)
            .apply()
    }

    companion object {
        private const val NAME = "nudge_prefs"
        private const val KEY_LAST_NUDGE_AT = "last_nudge_at"
        private const val KEY_LAST_APP_OPEN_AT = "last_app_open_at"
        private const val KEY_NUDGE_DAY = "nudge_day"
        private const val KEY_NUDGE_COUNT_TODAY = "nudge_count_today"
    }
}
