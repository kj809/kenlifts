package com.kenlifts.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.room.ExerciseWeightEntity
import com.kenlifts.data.room.WorkoutSetEntity
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.ExerciseWeightRepository
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository
import com.kenlifts.repository.WorkoutSetRepository
import com.kenlifts.progression.ProgressionRules
import com.kenlifts.util.roundToNearest2_5
import com.kenlifts.progression.ProgressionService
import com.kenlifts.service.timer.TimerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WorkoutExerciseItem(
    val exerciseId: Long,
    val routineExerciseId: Long?,
    val exerciseName: String,
    val sets: Int,
    val reps: Int,
    val restSeconds: Int,
    val weightKg: Float?,
    /** Consecutive failures at current weight. 0 = no recent failures. */
    val consecutiveFailures: Int = 0,
    /** Preview of next weight on success (e.g. "Next: 25 kg"). Or deload warning if 2 failures (e.g. "Deload to 22.5 kg if you fail"). */
    val nextWeightPreview: String? = null
)

data class WorkoutSessionState(
    val workoutId: Long? = null,
    val routineName: String? = null,
    val exercises: List<WorkoutExerciseItem> = emptyList(),
    val currentExerciseIndex: Int = 0,
    /** exerciseId -> (setIndex -> repsCompleted). Absent = empty/incomplete. 1-5 = that many reps. */
    val setReps: Map<Long, Map<Int, Int>> = emptyMap()
)

class WorkoutSessionViewModel(
    application: Application,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseWeightRepository: ExerciseWeightRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val progressionService: ProgressionService
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(WorkoutSessionState())
    val state: StateFlow<WorkoutSessionState> = _state.asStateFlow()

    fun startWorkout(routineId: Long) {
        viewModelScope.launch {
            val routine = routineRepository.getRoutineById(routineId)
            val routineExercises = routineExerciseRepository.getExercisesByRoutineIdSync(routineId)
            var workout = workoutRepository.getActiveWorkout()
            if (workout == null || workout.routineId != routineId) {
                workout = null
            }
            val workoutId = workout?.id ?: workoutRepository.insertWorkout(
                com.kenlifts.data.room.WorkoutEntity(
                    routineId = routineId,
                    startedAt = System.currentTimeMillis()
                )
            )
            val existingSets = workoutId.let { workoutSetRepository.getSetsByWorkoutIdSync(it) }
            val setRepsMap = existingSets.groupBy { it.exerciseId }.mapValues { (_, sets) ->
                sets.associate { it.setIndex to (it.repsCompleted ?: 5) }
            }
            val items = routineExercises.map { re ->
                val exercise = exerciseRepository.getExerciseById(re.exerciseId)
                val weight = exerciseWeightRepository.getWeightByExerciseIdSync(re.exerciseId)
                val currentKg = weight?.weightKg ?: 0f
                val fails = weight?.consecutiveFailures ?: 0
                val nextPreview = when {
                    fails >= ProgressionRules.deloadFailuresThreshold - 1 -> {
                        val deloaded = ProgressionRules.deloadedWeight(currentKg)
                        "Deload to %.1f kg if you fail".format(deloaded)
                    }
                    currentKg > 0f -> {
                        val inc = weight?.incrementKg ?: ProgressionRules.getIncrementKg(re.exerciseId)
                        val next = roundToNearest2_5(currentKg + inc)
                        "Next: %.1f kg".format(next)
                    }
                    else -> null
                }
                WorkoutExerciseItem(
                    exerciseId = re.exerciseId,
                    routineExerciseId = re.id,
                    exerciseName = exercise?.name ?: "?",
                    sets = re.sets,
                    reps = re.reps,
                    restSeconds = re.restSeconds,
                    weightKg = weight?.weightKg,
                    consecutiveFailures = fails,
                    nextWeightPreview = nextPreview
                )
            }
            _state.update {
                it.copy(
                    workoutId = workoutId,
                    routineName = routine?.name,
                    exercises = items,
                    currentExerciseIndex = 0,
                    setReps = setRepsMap
                )
            }
        }
    }

    /** Cycle: empty -> 5 -> 4 -> 3 -> 2 -> 1 -> empty -> 5... */
    fun cycleSetReps(exerciseId: Long, setIndex: Int) {
        viewModelScope.launch {
            val s = _state.value
            val workoutId = s.workoutId ?: return@launch
            val exercise = s.exercises.find { it.exerciseId == exerciseId } ?: return@launch
            val currentReps = s.setReps[exerciseId]?.get(setIndex)

            val nextReps = when (currentReps) {
                null -> 5      // empty -> 5
                5 -> 4
                4 -> 3
                3 -> 2
                2 -> 1
                1 -> null     // 1 -> empty
                else -> 5     // legacy/invalid -> 5
            }

            if (nextReps == null) {
                workoutSetRepository.deleteSet(workoutId, exerciseId, setIndex)
                _state.update {
                    val current = it.setReps[exerciseId].orEmpty().toMutableMap()
                    current.remove(setIndex)
                    it.copy(setReps = it.setReps + (exerciseId to current))
                }
            } else {
                workoutSetRepository.deleteSet(workoutId, exerciseId, setIndex)
                workoutSetRepository.insertWorkoutSet(
                    WorkoutSetEntity(
                        workoutId = workoutId,
                        exerciseId = exerciseId,
                        routineExerciseId = exercise.routineExerciseId,
                        setIndex = setIndex,
                        repsCompleted = nextReps,
                        weightKg = exercise.weightKg
                    )
                )
                _state.update {
                    val current = it.setReps[exerciseId].orEmpty().toMutableMap()
                    current[setIndex] = nextReps
                    it.copy(setReps = it.setReps + (exerciseId to current))
                }
                if (nextReps == 5) {
                    TimerManager.start(getApplication(), exercise.restSeconds)
                }
                val wasNewSet = setIndex !in (s.setReps[exerciseId].orEmpty().keys)
                val loggedCount = (s.setReps[exerciseId].orEmpty().keys + setIndex).size
                if (wasNewSet && loggedCount == exercise.sets) {
                    advanceToNextExercise()
                }
            }
        }
    }

    fun resetRestTimer() {
        val s = TimerManager.state.value
        if (s.active && s.totalSeconds > 0) {
            TimerManager.reset(getApplication())
        }
    }

    fun stopRestTimer() {
        TimerManager.stop(getApplication())
    }

    private fun advanceToNextExercise() {
        _state.update {
            val next = it.currentExerciseIndex + 1
            if (next >= it.exercises.size) {
                completeWorkout()
            }
            it.copy(currentExerciseIndex = next.coerceAtMost(it.exercises.size))
        }
    }

    fun updateWeight(exerciseId: Long, weightKg: Float?) {
        viewModelScope.launch {
            val existing = exerciseWeightRepository.getWeightByExerciseIdSync(exerciseId)
            val entity = if (existing != null) {
                existing.copy(weightKg = weightKg)
            } else {
                val inc = ProgressionRules.getIncrementKg(exerciseId)
                ExerciseWeightEntity(
                    exerciseId = exerciseId,
                    weightKg = weightKg,
                    incrementKg = inc,
                    consecutiveFailures = 0,
                    lastAttemptWeightKg = null
                )
            }
            exerciseWeightRepository.upsertExerciseWeight(entity)
            _state.update {
                it.copy(
                    exercises = it.exercises.map { ex ->
                        if (ex.exerciseId == exerciseId) ex.copy(weightKg = weightKg) else ex
                    }
                )
            }
        }
    }

    fun getSetReps(exerciseId: Long, setIndex: Int): Int? =
        _state.value.setReps[exerciseId]?.get(setIndex)

    fun isSetCompleted(exerciseId: Long, setIndex: Int): Boolean =
        _state.value.setReps[exerciseId]?.get(setIndex) != null

    fun completeWorkout() {
        viewModelScope.launch {
            val s = _state.value
            val workoutId = s.workoutId ?: return@launch
            val workout = workoutRepository.getWorkoutById(workoutId) ?: return@launch
            val routineId = workout.routineId
            progressionService.applyProgression(workoutId, routineId)
            workoutRepository.updateWorkout(
                workout.copy(completedAt = System.currentTimeMillis())
            )
        }
    }
}
