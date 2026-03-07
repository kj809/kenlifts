package com.kenlifts.repository

import com.kenlifts.data.room.RoutineDao
import com.kenlifts.data.room.RoutineEntity
import kotlinx.coroutines.flow.Flow

class RoutineRepository(private val routineDao: RoutineDao) {
    val allRoutines: Flow<List<RoutineEntity>> = routineDao.getAllRoutines()

    suspend fun getRoutineById(id: Long) = routineDao.getRoutineById(id)

    suspend fun insertRoutine(routine: RoutineEntity) = routineDao.insertRoutine(routine)
}
