package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.WorkoutSetEntity
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository
import com.kenlifts.repository.WorkoutSetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExerciseSetDetail(
    val exerciseName: String,
    val expectedSets: Int,
    val expectedReps: Int,
    val completedSets: Map<Int, WorkoutSetEntity>
)

data class WorkoutDetailState(
    val routineName: String = "",
    val startedAt: Long = 0,
    val completedAt: Long? = null,
    val exerciseDetails: List<ExerciseSetDetail> = emptyList()
)

class WorkoutDetailViewModel(
    private val workoutRepository: WorkoutRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private val _detail = MutableStateFlow<WorkoutDetailState?>(null)
    val detail: StateFlow<WorkoutDetailState?> = _detail.asStateFlow()

    fun loadWorkout(workoutId: Long) {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId) ?: return@launch
            val routine = routineRepository.getRoutineById(workout.routineId)
            val routineExercises = routineExerciseRepository.getExercisesByRoutineIdSync(workout.routineId)
            val sets = workoutSetRepository.getSetsByWorkoutIdSync(workoutId)
            val setsByExerciseId = sets.groupBy { it.exerciseId }

            val exerciseDetails = routineExercises.map { re ->
                val exerciseName = exerciseRepository.getExerciseById(re.exerciseId)?.name ?: "?"
                val completedSets = (setsByExerciseId[re.exerciseId] ?: emptyList())
                    .associateBy { it.setIndex }
                ExerciseSetDetail(
                    exerciseName = exerciseName,
                    expectedSets = re.sets,
                    expectedReps = re.reps,
                    completedSets = completedSets
                )
            }
            _detail.update {
                WorkoutDetailState(
                    routineName = routine?.name ?: "?",
                    startedAt = workout.startedAt,
                    completedAt = workout.completedAt,
                    exerciseDetails = exerciseDetails
                )
            }
        }
    }
}
