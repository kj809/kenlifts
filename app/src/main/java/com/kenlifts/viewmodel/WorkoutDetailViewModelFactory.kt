package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository
import com.kenlifts.repository.WorkoutSetRepository

class WorkoutDetailViewModelFactory(
    private val workoutRepository: WorkoutRepository,
    private val workoutSetRepository: WorkoutSetRepository,
    private val routineRepository: RoutineRepository,
    private val routineExerciseRepository: RoutineExerciseRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java)) {
            return WorkoutDetailViewModel(
                workoutRepository,
                workoutSetRepository,
                routineRepository,
                routineExerciseRepository,
                exerciseRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
