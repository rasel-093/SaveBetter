package com.example.savebetter.core.data.mapper

import com.example.savebetter.core.data.local.entity.MonthlyTargetEntity
import com.example.savebetter.core.data.local.entity.SalaryHandRecordEntity
import com.example.savebetter.core.data.local.entity.WeeklyTargetEntity
import com.example.savebetter.core.data.remote.firebase.dto.MonthlyTargetFirestoreDto
import com.example.savebetter.core.data.remote.firebase.dto.SalaryHandRecordFirestoreDto
import com.example.savebetter.core.data.remote.firebase.dto.WeeklyTargetFirestoreDto
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.WeeklyTarget
import java.time.Instant

object TargetMapper {

    // Weekly Target
    fun toDomain(entity: WeeklyTargetEntity): WeeklyTarget =
        WeeklyTarget(
            id = entity.id,
            userId = entity.userId,
            weekStart = entity.weekStart,
            weekEnd = entity.weekEnd,
            targetAmountMinor = entity.targetAmountMinor,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus
        )

    fun toEntity(domain: WeeklyTarget): WeeklyTargetEntity =
        WeeklyTargetEntity(
            id = domain.id,
            userId = domain.userId,
            weekStart = domain.weekStart,
            weekEnd = domain.weekEnd,
            targetAmountMinor = domain.targetAmountMinor,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus
        )

    fun toDomain(dto: WeeklyTargetFirestoreDto): WeeklyTarget =
        WeeklyTarget(
            id = dto.id,
            userId = dto.userId,
            weekStart = dto.weekStart,
            weekEnd = dto.weekEnd,
            targetAmountMinor = dto.targetAmountMinor,
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED
        )

    fun toFirestoreDto(domain: WeeklyTarget): WeeklyTargetFirestoreDto =
        WeeklyTargetFirestoreDto(
            id = domain.id,
            userId = domain.userId,
            weekStart = domain.weekStart,
            weekEnd = domain.weekEnd,
            targetAmountMinor = domain.targetAmountMinor,
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli()
        )

    // Monthly Target
    fun toDomain(entity: MonthlyTargetEntity): MonthlyTarget =
        MonthlyTarget(
            id = entity.id,
            userId = entity.userId,
            month = entity.month,
            year = entity.year,
            targetAmountMinor = entity.targetAmountMinor,
            savingGoalMinor = entity.savingGoalMinor,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus
        )

    fun toEntity(domain: MonthlyTarget): MonthlyTargetEntity =
        MonthlyTargetEntity(
            id = domain.id,
            userId = domain.userId,
            month = domain.month,
            year = domain.year,
            targetAmountMinor = domain.targetAmountMinor,
            savingGoalMinor = domain.savingGoalMinor,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus
        )

    fun toDomain(dto: MonthlyTargetFirestoreDto): MonthlyTarget =
        MonthlyTarget(
            id = dto.id,
            userId = dto.userId,
            month = dto.month,
            year = dto.year,
            targetAmountMinor = dto.targetAmountMinor,
            savingGoalMinor = dto.savingGoalMinor,
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED
        )

    fun toFirestoreDto(domain: MonthlyTarget): MonthlyTargetFirestoreDto =
        MonthlyTargetFirestoreDto(
            id = domain.id,
            userId = domain.userId,
            month = domain.month,
            year = domain.year,
            targetAmountMinor = domain.targetAmountMinor,
            savingGoalMinor = domain.savingGoalMinor,
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli()
        )

    // Salary Hand Record
    fun toDomain(entity: SalaryHandRecordEntity): SalaryHandRecord =
        SalaryHandRecord(
            id = entity.id,
            userId = entity.userId,
            month = entity.month,
            year = entity.year,
            salaryAmountMinor = entity.salaryAmountMinor,
            handRemainingAmountMinor = entity.handRemainingAmountMinor,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus
        )

    fun toEntity(domain: SalaryHandRecord): SalaryHandRecordEntity =
        SalaryHandRecordEntity(
            id = domain.id,
            userId = domain.userId,
            month = domain.month,
            year = domain.year,
            salaryAmountMinor = domain.salaryAmountMinor,
            handRemainingAmountMinor = domain.handRemainingAmountMinor,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus
        )

    fun toDomain(dto: SalaryHandRecordFirestoreDto): SalaryHandRecord =
        SalaryHandRecord(
            id = dto.id,
            userId = dto.userId,
            month = dto.month,
            year = dto.year,
            salaryAmountMinor = dto.salaryAmountMinor,
            handRemainingAmountMinor = dto.handRemainingAmountMinor,
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED
        )

    fun toFirestoreDto(domain: SalaryHandRecord): SalaryHandRecordFirestoreDto =
        SalaryHandRecordFirestoreDto(
            id = domain.id,
            userId = domain.userId,
            month = domain.month,
            year = domain.year,
            salaryAmountMinor = domain.salaryAmountMinor,
            handRemainingAmountMinor = domain.handRemainingAmountMinor,
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli()
        )
}

fun WeeklyTargetEntity.toDomain(): WeeklyTarget = TargetMapper.toDomain(this)
fun WeeklyTarget.toEntity(): WeeklyTargetEntity = TargetMapper.toEntity(this)
fun WeeklyTargetFirestoreDto.toDomain(): WeeklyTarget = TargetMapper.toDomain(this)
fun WeeklyTarget.toFirestoreDto(): WeeklyTargetFirestoreDto = TargetMapper.toFirestoreDto(this)

fun MonthlyTargetEntity.toDomain(): MonthlyTarget = TargetMapper.toDomain(this)
fun MonthlyTarget.toEntity(): MonthlyTargetEntity = TargetMapper.toEntity(this)
fun MonthlyTargetFirestoreDto.toDomain(): MonthlyTarget = TargetMapper.toDomain(this)
fun MonthlyTarget.toFirestoreDto(): MonthlyTargetFirestoreDto = TargetMapper.toFirestoreDto(this)

fun SalaryHandRecordEntity.toDomain(): SalaryHandRecord = TargetMapper.toDomain(this)
fun SalaryHandRecord.toEntity(): SalaryHandRecordEntity = TargetMapper.toEntity(this)
fun SalaryHandRecordFirestoreDto.toDomain(): SalaryHandRecord = TargetMapper.toDomain(this)
fun SalaryHandRecord.toFirestoreDto(): SalaryHandRecordFirestoreDto = TargetMapper.toFirestoreDto(this)
