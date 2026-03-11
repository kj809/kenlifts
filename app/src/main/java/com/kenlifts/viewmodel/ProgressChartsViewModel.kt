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

data class ExerciseChartData(
    val exerciseId: Long,
    val exerciseName: String,
    val data: List<WorkoutWeightPoint>
)

data class ProgressChartsState(
    val exercises: List<com.kenlifts.data.room.ExerciseEntity> = emptyList(),
    val selectedExerciseIds: Set<Long> = emptySet(),
    val range: ChartRange = ChartRange.ALL,
    val chartDataByExercise: List<ExerciseChartData> = emptyList()
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
            val current = _state.value.selectedExerciseIds
            if (current.isEmpty() && exercises.isNotEmpty()) {
                toggleExerciseSelection(exercises.first().id)
            }
        }
    }

    fun toggleExerciseSelection(exerciseId: Long) {
        viewModelScope.launch {
            _state.update {
                val next = if (exerciseId in it.selectedExerciseIds) {
                    it.selectedExerciseIds - exerciseId
                } else {
                    it.selectedExerciseIds + exerciseId
                }
                it.copy(selectedExerciseIds = next)
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
            val ids = _state.value.selectedExerciseIds
            if (ids.isEmpty()) {
                _state.update { it.copy(chartDataByExercise = emptyList()) }
                return@launch
            }
            val range = _state.value.range
            val cutoff = range.months?.let { System.currentTimeMillis() - (it * 30L * 24 * 60 * 60 * 1000) }
            val chartDataByExercise = ids.mapNotNull { exerciseId ->
                val exercise = exerciseRepository.getExerciseById(exerciseId) ?: return@mapNotNull null
                var data = workoutSetRepository.getWeightTimeSeriesByExercise(exerciseId)
                if (cutoff != null) data = data.filter { it.startedAt >= cutoff }
                ExerciseChartData(exerciseId, exercise.name, data)
            }
            _state.update { it.copy(chartDataByExercise = chartDataByExercise) }
        }
    }
}
