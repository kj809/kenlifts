package com.kenlifts.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseWeightDao {
    @Query("SELECT * FROM exercise_weights WHERE exerciseId = :exerciseId LIMIT 1")
    fun getWeightByExerciseId(exerciseId: Long): Flow<ExerciseWeightEntity?>

    @Query("SELECT * FROM exercise_weights")
    fun getAllWeights(): Flow<List<ExerciseWeightEntity>>

    @Query("SELECT * FROM exercise_weights WHERE exerciseId = :exerciseId LIMIT 1")
    suspend fun getWeightByExerciseIdSync(exerciseId: Long): ExerciseWeightEntity?

    @Upsert
    suspend fun upsertExerciseWeight(weight: ExerciseWeightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseWeights(weights: List<ExerciseWeightEntity>)
}
