package com.kenlifts.repository

import com.kenlifts.data.room.ExerciseDao
import com.kenlifts.data.room.ExerciseEntity
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(private val exerciseDao: ExerciseDao) {
    val allExercises: Flow<List<ExerciseEntity>> = exerciseDao.getAllExercises()

    suspend fun getAllExercisesSync() = exerciseDao.getAllExercisesSync()

    suspend fun getExerciseById(id: Long) = exerciseDao.getExerciseById(id)

    suspend fun getExerciseByName(name: String) = exerciseDao.getExerciseByName(name)

    suspend fun getExercisesNotInRoutine(routineId: Long) = exerciseDao.getExercisesNotInRoutine(routineId)

    suspend fun insertExercise(exercise: ExerciseEntity) = exerciseDao.insertExercise(exercise)
}
