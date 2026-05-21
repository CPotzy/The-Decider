package com.cpotzy.thedecider.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.cpotzy.thedecider.data.db.dao.CompletionDao
import com.cpotzy.thedecider.data.db.dao.SnoozeDao
import com.cpotzy.thedecider.data.db.dao.StepDao
import com.cpotzy.thedecider.data.db.dao.TaskDao
import com.cpotzy.thedecider.data.db.entities.CompletionEntity
import com.cpotzy.thedecider.data.db.entities.SnoozeEntity
import com.cpotzy.thedecider.data.db.entities.StepEntity
import com.cpotzy.thedecider.data.db.entities.TaskEntity

@Database(
    entities = [TaskEntity::class, StepEntity::class, CompletionEntity::class, SnoozeEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun stepDao(): StepDao
    abstract fun completionDao(): CompletionDao
    abstract fun snoozeDao(): SnoozeDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "thedecider.db").build()
    }
}
