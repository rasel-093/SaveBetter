package com.example.savebetter.core.data.local

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Room TypeConverters for types that Room cannot store natively.
 *
 * Currently converts:
 *  - [Instant] ↔ [Long] (epoch milliseconds)
 *
 * More converters will be added in Step 4 as entities are defined.
 */
class SaveBetterTypeConverters {

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }
}
