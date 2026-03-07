package com.kenlifts.repository

import com.kenlifts.data.room.RoutineExerciseDao
import com.kenlifts.data.room.RoutineExerciseEntity
import kotlinx.coroutines.flow.Flow

class RoutineExerciseRepository(private val routineExerciseDao: RoutineExerciseDao) {
    fun getExercisesByRoutineId(routineId: Long): Flow<List<RoutineExerciseEntity>> =
        routineExerciseDao.getExercisesByRoutineId(routineId)

    suspend fun getExercisesByRoutineIdSync(routineId: Long) =
        routineExerciseDao.getExercisesByRoutineIdSync(routineId)

    suspend fun insertRoutineExercise(routineExercise: RoutineExerciseEntity) =
        routineExerciseDao.insertRoutineExercise(routineExercise)

    suspend fun updateRoutineExercise(routineExercise: RoutineExerciseEntity) =
        routineExerciseDao.updateRoutineExercise(routineExercise)

    suspend fun deleteRoutineExercise(routineExercise: RoutineExerciseEntity) =
        routineExerciseDao.deleteRoutineExercise(routineExercise)

    suspend fun deleteById(id: Long) = routineExerciseDao.deleteById(id)
}
