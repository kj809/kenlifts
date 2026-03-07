package com.kenlifts.progression

import com.kenlifts.data.room.RoutineExerciseEntity
import com.kenlifts.repository.ExerciseWeightRepository
import com.kenlifts.util.roundToNearest2_5
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.WorkoutSetRepository

/**
 * Applies StrongLifts-style progression when a workout is completed.
 * For each exercise in the workout: if all work sets done at target reps → add weight;
 * else increment consecutiveFailures; if failures ≥ 3 at same weight → deload 10%.
 */
class ProgressionService(
    private val workoutSetRepository: WorkoutSetRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val exerciseWeightRepository: ExerciseWeightRepository
) {

    suspend fun applyProgression(workoutId: Long, routineId: Long) {
        val sets = workoutSetRepository.getSetsByWorkoutIdSync(workoutId)
        val routineExercises = routineExerciseRepository.getExercisesByRoutineIdSync(routineId)
            .groupBy { it.exerciseId }

        for ((exerciseId, setsForExercise) in sets.groupBy { it.exerciseId }) {
            val routineEx = routineExercises[exerciseId]?.firstOrNull() ?: continue
            val completedCount = setsForExercise.count { it.repsCompleted != null && it.repsCompleted >= routineEx.reps }
            val allCompleted = completedCount >= routineEx.sets
            val weightEntity = exerciseWeightRepository.getWeightByExerciseIdSync(exerciseId)
                ?: continue
            val currentKg = weightEntity.weightKg ?: 0f
            val increment = weightEntity.incrementKg ?: ProgressionRules.getIncrementKg(exerciseId)

            val newEntity = when {
                allCompleted -> {
                    val newWeight = currentKg + increment
                    weightEntity.copy(
                        weightKg = roundToNearest2_5(newWeight),
                        consecutiveFailures = 0,
                        lastAttemptWeightKg = currentKg
                    )
                }
                weightEntity.consecutiveFailures + 1 >= ProgressionRules.deloadFailuresThreshold -> {
                    val deloaded = ProgressionRules.deloadedWeight(currentKg)
                    weightEntity.copy(
                        weightKg = deloaded,
                        consecutiveFailures = 0,
                        lastAttemptWeightKg = currentKg
                    )
                }
                else -> {
                    weightEntity.copy(
                        consecutiveFailures = weightEntity.consecutiveFailures + 1,
                        lastAttemptWeightKg = currentKg
                    )
                }
            }
            exerciseWeightRepository.upsertExerciseWeight(newEntity)
        }
    }
}
