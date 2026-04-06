package com.rohitkhandelwal.tickr.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitkhandelwal.tickr.data.local.dao.TaskDao
import com.rohitkhandelwal.tickr.data.local.entity.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class TickrDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
