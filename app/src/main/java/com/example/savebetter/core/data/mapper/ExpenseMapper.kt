package com.example.savebetter.core.data.mapper

import com.example.savebetter.core.data.local.entity.ExpenseEntity
import com.example.savebetter.core.data.remote.firebase.dto.ExpenseFirestoreDto
import com.example.savebetter.core.domain.model.Expense
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

object ExpenseMapper {

    fun toDomain(entity: ExpenseEntity): Expense =
        Expense(
            id = entity.id,
            userId = entity.userId,
            amountMinor = entity.amountMinor,
            categoryId = entity.categoryId,
            note = entity.note,
            date = entity.date,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus,
            isDeleted = entity.isDeleted,
            deletedAt = entity.deletedAt
        )

    fun toEntity(domain: Expense): ExpenseEntity =
        ExpenseEntity(
            id = domain.id,
            userId = domain.userId,
            amountMinor = domain.amountMinor,
            categoryId = domain.categoryId,
            note = domain.note,
            date = domain.date,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus,
            isDeleted = domain.isDeleted,
            deletedAt = domain.deletedAt
        )

    fun toDomain(dto: ExpenseFirestoreDto): Expense =
        Expense(
            id = dto.id,
            userId = dto.userId,
            amountMinor = dto.amountMinor,
            categoryId = dto.categoryId,
            note = dto.note,
            date = Instant.ofEpochMilli(dto.dateEpochMilli),
            createdAt = Instant.ofEpochMilli(dto.createdAtEpochMilli),
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED,
            isDeleted = dto.isDeleted,
            deletedAt = dto.deletedAtEpochMilli?.let { Instant.ofEpochMilli(it) }
        )

    fun toFirestoreDto(domain: Expense): ExpenseFirestoreDto =
        ExpenseFirestoreDto(
            id = domain.id,
            userId = domain.userId,
            amountMinor = domain.amountMinor,
            categoryId = domain.categoryId,
            note = domain.note,
            dateEpochMilli = domain.date.toEpochMilli(),
            createdAtEpochMilli = domain.createdAt.toEpochMilli(),
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli(),
            isDeleted = domain.isDeleted,
            deletedAtEpochMilli = domain.deletedAt?.toEpochMilli()
        )
}

fun ExpenseEntity.toDomain(): Expense = ExpenseMapper.toDomain(this)
fun Expense.toEntity(): ExpenseEntity = ExpenseMapper.toEntity(this)
fun ExpenseFirestoreDto.toDomain(): Expense = ExpenseMapper.toDomain(this)
fun Expense.toFirestoreDto(): ExpenseFirestoreDto = ExpenseMapper.toFirestoreDto(this)
