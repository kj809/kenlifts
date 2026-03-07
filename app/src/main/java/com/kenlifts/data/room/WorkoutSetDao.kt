package com.kenlifts.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSetDao {
    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY exerciseId, setIndex")
    fun getSetsByWorkoutId(workoutId: Long): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY exerciseId, setIndex")
    suspend fun getSetsByWorkoutIdSync(workoutId: Long): List<WorkoutSetEntity>

    @Insert
    suspend fun insertWorkoutSet(workoutSet: WorkoutSetEntity): Long

    @Insert
    suspend fun insertWorkoutSets(workoutSets: List<WorkoutSetEntity>)

    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId AND setIndex = :setIndex")
    suspend fun deleteSet(workoutId: Long, exerciseId: Long, setIndex: Int)

    @Query("""
        SELECT w.startedAt as startedAt, MAX(ws.weightKg) as maxWeightKg
        FROM workout_sets ws
        INNER JOIN workouts w ON ws.workoutId = w.id
        WHERE ws.exerciseId = :exerciseId AND ws.weightKg IS NOT NULL
        GROUP BY w.id
        ORDER BY w.startedAt ASC
    """)
    suspend fun getWeightTimeSeriesByExercise(exerciseId: Long): List<WorkoutWeightPoint>
}

data class WorkoutWeightPoint(
    val startedAt: Long,
    val maxWeightKg: Float?
)
