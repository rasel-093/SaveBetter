package com.example.savebetter.core.data.remote.firebase

import com.example.savebetter.core.data.mapper.toDomain
import com.example.savebetter.core.data.mapper.toFirestoreDto
import com.example.savebetter.core.data.remote.TargetRemoteDataSource
import com.example.savebetter.core.data.remote.firebase.dto.MonthlyTargetFirestoreDto
import com.example.savebetter.core.data.remote.firebase.dto.SalaryHandRecordFirestoreDto
import com.example.savebetter.core.data.remote.firebase.dto.WeeklyTargetFirestoreDto
import com.example.savebetter.core.domain.model.MonthlyTarget
import com.example.savebetter.core.domain.model.SalaryHandRecord
import com.example.savebetter.core.domain.model.WeeklyTarget
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Firestore implementation for target and salary sync.
 */
@Singleton
class FirebaseTargetRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : TargetRemoteDataSource {

    override suspend fun fetchWeeklyTargets(userId: String): Result<List<WeeklyTarget>> = runCatching {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("weekly_targets")
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(WeeklyTargetFirestoreDto::class.java)?.toDomain()
        }
    }

    override suspend fun uploadWeeklyTarget(target: WeeklyTarget): Result<Unit> = runCatching {
        val dto = target.toFirestoreDto()
        firestore.collection("users")
            .document(target.userId)
            .collection("weekly_targets")
            .document(target.id)
            .set(dto, SetOptions.merge())
            .await()
    }

    override suspend fun fetchMonthlyTargets(userId: String): Result<List<MonthlyTarget>> = runCatching {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("monthly_targets")
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(MonthlyTargetFirestoreDto::class.java)?.toDomain()
        }
    }

    override suspend fun uploadMonthlyTarget(target: MonthlyTarget): Result<Unit> = runCatching {
        val dto = target.toFirestoreDto()
        firestore.collection("users")
            .document(target.userId)
            .collection("monthly_targets")
            .document(target.id)
            .set(dto, SetOptions.merge())
            .await()
    }

    override suspend fun fetchSalaryHandRecords(userId: String): Result<List<SalaryHandRecord>> = runCatching {
        val querySnapshot = firestore.collection("users")
            .document(userId)
            .collection("salary_hand_records")
            .get()
            .await()

        querySnapshot.documents.mapNotNull { doc ->
            doc.toObject(SalaryHandRecordFirestoreDto::class.java)?.toDomain()
        }
    }

    override suspend fun uploadSalaryHandRecord(record: SalaryHandRecord): Result<Unit> = runCatching {
        val dto = record.toFirestoreDto()
        firestore.collection("users")
            .document(record.userId)
            .collection("salary_hand_records")
            .document(record.id)
            .set(dto, SetOptions.merge())
            .await()
    }
}
