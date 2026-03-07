package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.WorkoutEntity
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class WorkoutWithRoutine(val workout: WorkoutEntity, val routineName: String)

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository
) : ViewModel() {
    val workoutsWithRoutineNames = combine(
        workoutRepository.allWorkouts,
        routineRepository.allRoutines
    ) { workouts, routines ->
        val routineMap = routines.associateBy { it.id }
        workouts.map { workout ->
            val routine = routineMap[workout.routineId]
            WorkoutWithRoutine(workout, routine?.name ?: "?")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
