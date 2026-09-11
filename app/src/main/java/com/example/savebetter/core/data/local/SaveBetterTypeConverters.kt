package com.example.savebetter.core.data.local

import androidx.room.TypeConverter
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

/**
 * Room TypeConverters for types that Room cannot store natively.
 */
class SaveBetterTypeConverters {

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun longToInstant(value: Long?): Instant? = value?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun syncStateToString(state: SyncState?): String? = state?.name

    @TypeConverter
    fun stringToSyncState(value: String?): SyncState? =
        value?.let { runCatching { SyncState.valueOf(it) }.getOrDefault(SyncState.SYNCED) }

    @TypeConverter
    fun debtDirectionToString(direction: DebtDirection?): String? = direction?.name

    @TypeConverter
    fun stringToDebtDirection(value: String?): DebtDirection? =
        value?.let { runCatching { DebtDirection.valueOf(it) }.getOrDefault(DebtDirection.RECEIVABLE) }
}
