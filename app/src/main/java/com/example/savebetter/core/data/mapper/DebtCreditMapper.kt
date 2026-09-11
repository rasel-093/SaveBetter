package com.example.savebetter.core.data.mapper

import com.example.savebetter.core.data.local.entity.DebtCreditEntity
import com.example.savebetter.core.data.remote.firebase.dto.DebtCreditFirestoreDto
import com.example.savebetter.core.domain.model.DebtCredit
import com.example.savebetter.core.domain.model.DebtDirection
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

object DebtCreditMapper {

    fun toDomain(entity: DebtCreditEntity): DebtCredit =
        DebtCredit(
            id = entity.id,
            userId = entity.userId,
            direction = entity.direction,
            personName = entity.personName,
            amountMinor = entity.amountMinor,
            note = entity.note,
            date = entity.date,
            dueDate = entity.dueDate,
            isSettled = entity.isSettled,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt
        )

    fun toEntity(domain: DebtCredit): DebtCreditEntity =
        DebtCreditEntity(
            id = domain.id,
            userId = domain.userId,
            direction = domain.direction,
            personName = domain.personName,
            amountMinor = domain.amountMinor,
            note = domain.note,
            date = domain.date,
            dueDate = domain.dueDate,
            isSettled = domain.isSettled,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus,
            isDeleted = domain.isDeleted,
            deletedAt = domain.deletedAt
        )

    fun toDomain(dto: DebtCreditFirestoreDto): DebtCredit =
        DebtCredit(
            id = dto.id,
            userId = dto.userId,
            direction = runCatching { DebtDirection.valueOf(dto.direction) }.getOrDefault(DebtDirection.RECEIVABLE),
            personName = dto.personName,
            amountMinor = dto.amountMinor,
            note = dto.note,
            date = Instant.ofEpochMilli(dto.dateEpochMilli),
            dueDate = dto.dueDateEpochMilli?.let { Instant.ofEpochMilli(it) },
            isSettled = dto.isSettled,
            createdAt = Instant.ofEpochMilli(dto.createdAtEpochMilli),
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED,
            isDeleted = dto.isDeleted,
            deletedAt = dto.deletedAtEpochMilli?.let { Instant.ofEpochMilli(it) }
        )

    fun toFirestoreDto(domain: DebtCredit): DebtCreditFirestoreDto =
        DebtCreditFirestoreDto(
            id = domain.id,
            userId = domain.userId,
            direction = domain.direction.name,
            personName = domain.personName,
            amountMinor = domain.amountMinor,
            note = domain.note,
            dateEpochMilli = domain.date.toEpochMilli(),
            dueDateEpochMilli = domain.dueDate?.toEpochMilli(),
            isSettled = domain.isSettled,
            createdAtEpochMilli = domain.createdAt.toEpochMilli(),
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli(),
            isDeleted = domain.isDeleted,
            deletedAtEpochMilli = domain.deletedAt?.toEpochMilli()
        )
}

fun DebtCreditEntity.toDomain(): DebtCredit = DebtCreditMapper.toDomain(this)
fun DebtCredit.toEntity(): DebtCreditEntity = DebtCreditMapper.toEntity(this)
fun DebtCreditFirestoreDto.toDomain(): DebtCredit = DebtCreditMapper.toDomain(this)
fun DebtCredit.toFirestoreDto(): DebtCreditFirestoreDto = DebtCreditMapper.toFirestoreDto(this)
