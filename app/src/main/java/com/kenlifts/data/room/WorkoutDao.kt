package com.kenlifts.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC")
    fun getAllWorkouts(): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: Long): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE routineId = :routineId ORDER BY startedAt DESC")
    fun getWorkoutsByRoutineId(routineId: Long): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts WHERE completedAt IS NULL LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)
}
