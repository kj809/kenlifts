package com.kenlifts.repository

import com.kenlifts.data.room.WorkoutSetDao
import com.kenlifts.data.room.WorkoutSetEntity
import kotlinx.coroutines.flow.Flow

class WorkoutSetRepository(private val workoutSetDao: WorkoutSetDao) {
    fun getSetsByWorkoutId(workoutId: Long): Flow<List<WorkoutSetEntity>> =
        workoutSetDao.getSetsByWorkoutId(workoutId)

    suspend fun getSetsByWorkoutIdSync(workoutId: Long) =
        workoutSetDao.getSetsByWorkoutIdSync(workoutId)

    suspend fun insertWorkoutSet(workoutSet: WorkoutSetEntity) =
        workoutSetDao.insertWorkoutSet(workoutSet)

    suspend fun insertWorkoutSets(workoutSets: List<WorkoutSetEntity>) =
        workoutSetDao.insertWorkoutSets(workoutSets)

    suspend fun deleteSet(workoutId: Long, exerciseId: Long, setIndex: Int) =
        workoutSetDao.deleteSet(workoutId, exerciseId, setIndex)

    suspend fun getWeightTimeSeriesByExercise(exerciseId: Long) =
        workoutSetDao.getWeightTimeSeriesByExercise(exerciseId)
}
