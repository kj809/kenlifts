package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.WorkoutEntity
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

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

    val workoutDates = workoutRepository.allWorkouts.map { workouts ->
        workouts.map { w ->
            val cal = Calendar.getInstance().apply { timeInMillis = w.startedAt }
            val dayCal = Calendar.getInstance()
            dayCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            dayCal.timeInMillis
        }.toSet()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )
}
