package com.kenlifts.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineExerciseDao {
    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex")
    fun getExercisesByRoutineId(routineId: Long): Flow<List<RoutineExerciseEntity>>

    @Query("SELECT * FROM routine_exercises WHERE routineId = :routineId ORDER BY orderIndex")
    suspend fun getExercisesByRoutineIdSync(routineId: Long): List<RoutineExerciseEntity>

    @Insert
    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity): Long

    @Insert
    suspend fun insertRoutineExercises(routineExercises: List<RoutineExerciseEntity>)

    @Update
    suspend fun updateRoutineExercise(routineExercise: RoutineExerciseEntity)

    @Delete
    suspend fun deleteRoutineExercise(routineExercise: RoutineExerciseEntity)

    @Query("DELETE FROM routine_exercises WHERE id = :id")
    suspend fun deleteById(id: Long)
}
