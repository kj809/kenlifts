package com.kenlifts.repository

import com.kenlifts.data.room.WorkoutDao
import com.kenlifts.data.room.WorkoutEntity
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: Flow<List<WorkoutEntity>> = workoutDao.getAllWorkouts()

    fun getWorkoutsByRoutineId(routineId: Long): Flow<List<WorkoutEntity>> =
        workoutDao.getWorkoutsByRoutineId(routineId)

    suspend fun getWorkoutById(id: Long) = workoutDao.getWorkoutById(id)

    suspend fun getActiveWorkout() = workoutDao.getActiveWorkout()

    suspend fun insertWorkout(workout: WorkoutEntity) = workoutDao.insertWorkout(workout)

    suspend fun updateWorkout(workout: WorkoutEntity) = workoutDao.updateWorkout(workout)
}
