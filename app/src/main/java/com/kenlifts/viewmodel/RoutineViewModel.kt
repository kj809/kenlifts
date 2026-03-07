package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.RoutineEntity
import com.kenlifts.repository.RoutineRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RoutineViewModel(private val routineRepository: RoutineRepository) : ViewModel() {
    val routines = routineRepository.allRoutines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun getRoutineById(id: Long) = routineRepository.getRoutineById(id)

    suspend fun insertRoutine(routine: RoutineEntity) = routineRepository.insertRoutine(routine)
}
