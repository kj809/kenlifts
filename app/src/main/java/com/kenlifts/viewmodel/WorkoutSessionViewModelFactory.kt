package com.kenlifts.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.ExerciseWeightRepository
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository
import com.kenlifts.repository.WorkoutSetRepository
import com.kenlifts.progression.ProgressionService

class WorkoutSessionViewModelFactory(
    private val application: Application,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val exerciseRepository: ExerciseRepository,
    private val exerciseWeightRepository: ExerciseWeightRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val progressionService: ProgressionService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutSessionViewModel::class.java)) {
            return WorkoutSessionViewModel(
                application,
                routineRepository,
                routineExerciseRepository,
                exerciseRepository,
                exerciseWeightRepository,
                workoutRepository,
                workoutSetRepository,
                progressionService
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
