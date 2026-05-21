package com.cpotzy.thedecider.work

import android.content.Context
import androidx.core.content.edit

class NudgeSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var windowStartHour: Int
        get() = prefs.getInt(KEY_WINDOW_START, DEFAULT_WINDOW_START).coerceIn(0, 23)
        set(value) { prefs.edit { putInt(KEY_WINDOW_START, value.coerceIn(0, 23)) } }

    var windowEndHour: Int
        get() = prefs.getInt(KEY_WINDOW_END, DEFAULT_WINDOW_END).coerceIn(1, 24)
        set(value) { prefs.edit { putInt(KEY_WINDOW_END, value.coerceIn(1, 24)) } }

    var maxPerDay: Int
        get() = prefs.getInt(KEY_MAX_PER_DAY, DEFAULT_MAX_PER_DAY).coerceIn(0, 10)
        set(value) { prefs.edit { putInt(KEY_MAX_PER_DAY, value.coerceIn(0, 10)) } }

    var minGapMinutes: Int
        get() = prefs.getInt(KEY_MIN_GAP, DEFAULT_MIN_GAP).coerceAtLeast(0)
        set(value) { prefs.edit { putInt(KEY_MIN_GAP, value.coerceAtLeast(0)) } }

    var quietAfterOpenMinutes: Int
        get() = prefs.getInt(KEY_QUIET_AFTER_OPEN, DEFAULT_QUIET_AFTER_OPEN).coerceAtLeast(0)
        set(value) { prefs.edit { putInt(KEY_QUIET_AFTER_OPEN, value.coerceAtLeast(0)) } }

    fun resetToDefaults() {
        windowStartHour = DEFAULT_WINDOW_START
        windowEndHour = DEFAULT_WINDOW_END
        maxPerDay = DEFAULT_MAX_PER_DAY
        minGapMinutes = DEFAULT_MIN_GAP
        quietAfterOpenMinutes = DEFAULT_QUIET_AFTER_OPEN
    }

    companion object {
        private const val NAME = "nudge_settings"
        private const val KEY_WINDOW_START = "window_start_hour"
        private const val KEY_WINDOW_END = "window_end_hour"
        private const val KEY_MAX_PER_DAY = "max_per_day"
        private const val KEY_MIN_GAP = "min_gap_minutes"
        private const val KEY_QUIET_AFTER_OPEN = "quiet_after_open_minutes"

        const val DEFAULT_WINDOW_START = 9
        const val DEFAULT_WINDOW_END = 21
        const val DEFAULT_MAX_PER_DAY = 3
        const val DEFAULT_MIN_GAP = 60
        const val DEFAULT_QUIET_AFTER_OPEN = 120
    }
}
