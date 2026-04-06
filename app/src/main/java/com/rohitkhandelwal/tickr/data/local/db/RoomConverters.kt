package com.rohitkhandelwal.tickr.data.local.db

import androidx.room.TypeConverter
import com.rohitkhandelwal.tickr.data.local.entity.SyncStateEntity

class RoomConverters {

    @TypeConverter
    fun fromSyncState(value: SyncStateEntity): String = value.name

    @TypeConverter
    fun toSyncState(value: String): SyncStateEntity = SyncStateEntity.valueOf(value)
}
