package com.example.savebetter.core.data.mapper

import com.example.savebetter.core.data.local.entity.UserEntity
import com.example.savebetter.core.data.remote.firebase.dto.UserFirestoreDto
import com.example.savebetter.core.domain.model.SyncState
import com.example.savebetter.core.domain.model.UserProfile
import java.time.Instant

object UserMapper {

    fun toDomain(entity: UserEntity): UserProfile =
        UserProfile(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            monthlySalaryMinor = entity.monthlySalaryMinor,
            preferredLanguage = entity.preferredLanguage,
            onboardingCompleted = entity.onboardingCompleted,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
            syncStatus = entity.syncStatus
        )

    fun toEntity(domain: UserProfile): UserEntity =
        UserEntity(
            id = domain.id,
            name = domain.name,
            email = domain.email,
            monthlySalaryMinor = domain.monthlySalaryMinor,
            preferredLanguage = domain.preferredLanguage,
            onboardingCompleted = domain.onboardingCompleted,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt,
            syncStatus = domain.syncStatus
        )

    fun toDomain(dto: UserFirestoreDto): UserProfile =
        UserProfile(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            monthlySalaryMinor = dto.monthlySalaryMinor,
            preferredLanguage = dto.preferredLanguage,
            onboardingCompleted = dto.onboardingCompleted,
            createdAt = Instant.ofEpochMilli(dto.createdAtEpochMilli),
            updatedAt = Instant.ofEpochMilli(dto.updatedAtEpochMilli),
            syncStatus = SyncState.SYNCED
        )

    fun toFirestoreDto(domain: UserProfile): UserFirestoreDto =
        UserFirestoreDto(
            id = domain.id,
            name = domain.name,
            email = domain.email,
            monthlySalaryMinor = domain.monthlySalaryMinor,
            preferredLanguage = domain.preferredLanguage,
            onboardingCompleted = domain.onboardingCompleted,
            createdAtEpochMilli = domain.createdAt.toEpochMilli(),
            updatedAtEpochMilli = domain.updatedAt.toEpochMilli()
        )
}

fun UserEntity.toDomain(): UserProfile = UserMapper.toDomain(this)
fun UserProfile.toEntity(): UserEntity = UserMapper.toEntity(this)
fun UserFirestoreDto.toDomain(): UserProfile = UserMapper.toDomain(this)
fun UserProfile.toFirestoreDto(): UserFirestoreDto = UserMapper.toFirestoreDto(this)
