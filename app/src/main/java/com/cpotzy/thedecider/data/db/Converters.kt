package com.cpotzy.thedecider.data.db

import androidx.room.TypeConverter
import com.cpotzy.thedecider.data.db.entities.CompletionType
import com.cpotzy.thedecider.data.db.entities.SnoozeKind
import com.cpotzy.thedecider.domain.model.Cadence
import com.cpotzy.thedecider.domain.model.Duration
import com.cpotzy.thedecider.domain.model.Energy
import com.cpotzy.thedecider.domain.model.TimeWindow
import java.time.Instant

class Converters {
    @TypeConverter fun instantToLong(v: Instant?): Long? = v?.toEpochMilli()
    @TypeConverter fun longToInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }

    @TypeConverter fun cadenceToString(v: Cadence): String = v.name
    @TypeConverter fun stringToCadence(v: String): Cadence = Cadence.valueOf(v)

    @TypeConverter fun energyToString(v: Energy): String = v.name
    @TypeConverter fun stringToEnergy(v: String): Energy = Energy.valueOf(v)

    @TypeConverter fun durationToString(v: Duration): String = v.name
    @TypeConverter fun stringToDuration(v: String): Duration = Duration.valueOf(v)

    @TypeConverter fun timeWindowToString(v: TimeWindow): String = v.name
    @TypeConverter fun stringToTimeWindow(v: String): TimeWindow = TimeWindow.valueOf(v)

    @TypeConverter fun completionTypeToString(v: CompletionType): String = v.name
    @TypeConverter fun stringToCompletionType(v: String): CompletionType = CompletionType.valueOf(v)

    @TypeConverter fun snoozeKindToString(v: SnoozeKind): String = v.name
    @TypeConverter fun stringToSnoozeKind(v: String): SnoozeKind = SnoozeKind.valueOf(v)
}
