package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.WorkoutSetRepository

class ProgressChartsViewModelFactory(
    private val exerciseRepository: ExerciseRepository,
    private val workoutSetRepository: WorkoutSetRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProgressChartsViewModel::class.java)) {
            return ProgressChartsViewModel(exerciseRepository, workoutSetRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
