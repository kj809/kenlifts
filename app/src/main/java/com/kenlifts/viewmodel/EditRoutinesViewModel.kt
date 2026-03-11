package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.RoutineExerciseEntity
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditRoutinesViewModel(
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    val routines = routineRepository.allRoutines.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedRoutineId = MutableStateFlow<Long?>(null)
    val selectedRoutineId: StateFlow<Long?> = _selectedRoutineId.asStateFlow()

    private val _routineExercises = MutableStateFlow<List<RoutineExerciseEntity>>(emptyList())
    val routineExercises: StateFlow<List<RoutineExerciseEntity>> = _routineExercises.asStateFlow()

    private val _availableExercises = MutableStateFlow<List<com.kenlifts.data.room.ExerciseEntity>>(emptyList())
    val availableExercises: StateFlow<List<com.kenlifts.data.room.ExerciseEntity>> = _availableExercises.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val exerciseNames = mutableMapOf<Long, String>()

    fun selectRoutine(id: Long) {
        _selectedRoutineId.value = id
    }

    fun loadRoutineExercises(routineId: Long) {
        viewModelScope.launch {
            val exercises = routineExerciseRepository.getExercisesByRoutineIdSync(routineId)
            exercises.forEach { re ->
                exerciseRepository.getExerciseById(re.exerciseId)?.let { ex ->
                    exerciseNames[ex.id] = ex.name
                }
            }
            _routineExercises.value = exercises
        }
    }

    fun showAddExerciseDialog() {
        viewModelScope.launch {
            val id = _selectedRoutineId.value ?: return@launch
            _availableExercises.value = exerciseRepository.getExercisesNotInRoutine(id)
            _showAddDialog.value = true
        }
    }

    fun dismissAddDialog() {
        _showAddDialog.value = false
    }

    fun addExerciseToRoutine(exercise: com.kenlifts.data.room.ExerciseEntity) {
        viewModelScope.launch {
            val routineId = _selectedRoutineId.value ?: return@launch
            val current = _routineExercises.value
            val nextOrder = current.maxOfOrNull { it.orderIndex }?.plus(1) ?: 0
            val sets = if (exercise.name == "Deadlift") 1 else 5
            val reps = 5
            routineExerciseRepository.insertRoutineExercise(
                RoutineExerciseEntity(
                    routineId = routineId,
                    exerciseId = exercise.id,
                    sets = sets,
                    reps = reps,
                    restSeconds = 120,
                    orderIndex = nextOrder
                )
            )
            loadRoutineExercises(routineId)
        }
    }

    fun isDeadlift(exerciseId: Long): Boolean = exerciseNames[exerciseId] == "Deadlift"

    fun updateSets(re: RoutineExerciseEntity, sets: Int) {
        viewModelScope.launch {
            routineExerciseRepository.updateRoutineExercise(re.copy(sets = sets))
            loadRoutineExercises(re.routineId)
        }
    }

    fun updateReps(re: RoutineExerciseEntity, reps: Int) {
        viewModelScope.launch {
            routineExerciseRepository.updateRoutineExercise(re.copy(reps = reps))
            loadRoutineExercises(re.routineId)
        }
    }

    fun updateRest(re: RoutineExerciseEntity, restSeconds: Int) {
        viewModelScope.launch {
            routineExerciseRepository.updateRoutineExercise(re.copy(restSeconds = restSeconds))
            loadRoutineExercises(re.routineId)
        }
    }

    fun deleteRoutineExercise(re: RoutineExerciseEntity) {
        viewModelScope.launch {
            routineExerciseRepository.deleteById(re.id)
            loadRoutineExercises(re.routineId)
        }
    }

    fun getExerciseName(exerciseId: Long): String = exerciseNames[exerciseId] ?: "?"
}
