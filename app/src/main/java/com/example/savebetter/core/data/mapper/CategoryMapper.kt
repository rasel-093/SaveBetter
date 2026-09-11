package com.example.savebetter.core.data.mapper

import com.example.savebetter.core.data.local.entity.CategoryEntity
import com.example.savebetter.core.data.remote.firebase.dto.CategoryFirestoreDto
import com.example.savebetter.core.domain.model.Category
import com.example.savebetter.core.domain.model.SyncState
import java.time.Instant

object CategoryMapper {

    fun toDomain(entity: CategoryEntity): Category =
        Category(
            id = entity.id,
            userId = entity.userId,
            nameKey = entity.nameKey,
            customName = entity.customName,
            icon = entity.icon,
            colorToken = entity.colorToken,
            isDefault = entity.isDefault,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus,
            isDeleted = entity.isDeleted
        )

    fun toEntity(domain: Category): CategoryEntity =
        CategoryEntity(
            id = domain.id,
            userId = domain.userId,
            nameKey = domain.nameKey,
            customName = domain.customName,
            icon = domain.icon,
            colorToken = domain.colorToken,
            isDefault = domain.isDefault,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus,
            isDeleted = domain.isDeleted
        )

    fun toDomain(dto: CategoryFirestoreDto): Category =
        Category(
            id = dto.id,
            userId = dto.userId,
            nameKey = dto.nameKey,
            customName = dto.customName,
            icon = dto.icon,
            colorToken = dto.colorToken,
            isDefault = dto.isDefault,
            createdAt = Instant.ofEpochMilli(dto.createdAtEpochMilli),
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED,
            isDeleted = dto.isDeleted
        )

    fun toFirestoreDto(domain: Category): CategoryFirestoreDto =
        CategoryFirestoreDto(
            id = domain.id,
            userId = domain.userId,
            nameKey = domain.nameKey,
            customName = domain.customName,
            icon = domain.icon,
            colorToken = domain.colorToken,
            isDefault = domain.isDefault,
            createdAtEpochMilli = domain.createdAt.toEpochMilli(),
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli(),
            isDeleted = domain.isDeleted
        )
}

fun CategoryEntity.toDomain(): Category = CategoryMapper.toDomain(this)
fun Category.toEntity(): CategoryEntity = CategoryMapper.toEntity(this)
fun CategoryFirestoreDto.toDomain(): Category = CategoryMapper.toDomain(this)
fun Category.toFirestoreDto(): CategoryFirestoreDto = CategoryMapper.toFirestoreDto(this)
