package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.WorkoutWeightPoint
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.WorkoutSetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ChartRange(val months: Int?) {
    ONE_MONTH(1),
    THREE_MONTHS(3),
    SIX_MONTHS(6),
    ALL(null)
}

data class ProgressChartsState(
    val exercises: List<com.kenlifts.data.room.ExerciseEntity> = emptyList(),
    val selectedExerciseId: Long? = null,
    val selectedExerciseName: String = "",
    val range: ChartRange = ChartRange.ALL,
    val chartData: List<WorkoutWeightPoint> = emptyList()
)

class ProgressChartsViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutSetRepository: WorkoutSetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressChartsState())
    val state: StateFlow<ProgressChartsState> = _state.asStateFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            val exercises = exerciseRepository.getAllExercisesSync()
            _state.update { it.copy(exercises = exercises) }
            val currentId = _state.value.selectedExerciseId
            if (currentId == null && exercises.isNotEmpty()) {
                selectExercise(exercises.first().id)
            }
        }
    }

    fun selectExercise(exerciseId: Long) {
        viewModelScope.launch {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            _state.update {
                it.copy(
                    selectedExerciseId = exerciseId,
                    selectedExerciseName = exercise?.name ?: "?"
                )
            }
            loadChartData()
        }
    }

    fun setRange(range: ChartRange) {
        _state.update { it.copy(range = range) }
        loadChartData()
    }

    private fun loadChartData() {
        viewModelScope.launch {
            val exerciseId = _state.value.selectedExerciseId ?: return@launch
            var data = workoutSetRepository.getWeightTimeSeriesByExercise(exerciseId)
            val range = _state.value.range
            if (range.months != null) {
                val cutoff = System.currentTimeMillis() - (range.months * 30L * 24 * 60 * 60 * 1000)
                data = data.filter { it.startedAt >= cutoff }
            }
            _state.update { it.copy(chartData = data) }
        }
    }
}
