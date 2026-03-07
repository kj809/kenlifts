package com.kenlifts.repository

import com.kenlifts.data.room.ExerciseWeightDao
import com.kenlifts.data.room.ExerciseWeightEntity
import kotlinx.coroutines.flow.Flow

class ExerciseWeightRepository(private val exerciseWeightDao: ExerciseWeightDao) {
    fun getWeightByExerciseId(exerciseId: Long): Flow<ExerciseWeightEntity?> =
        exerciseWeightDao.getWeightByExerciseId(exerciseId)

    val allWeights: Flow<List<ExerciseWeightEntity>> = exerciseWeightDao.getAllWeights()

    suspend fun getWeightByExerciseIdSync(exerciseId: Long) =
        exerciseWeightDao.getWeightByExerciseIdSync(exerciseId)

    suspend fun upsertExerciseWeight(weight: ExerciseWeightEntity) =
        exerciseWeightDao.upsertExerciseWeight(weight)
}
