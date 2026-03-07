package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutRepository

class HistoryViewModelFactory(
    private val workoutRepository: WorkoutRepository,
    private val routineRepository: RoutineRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            return HistoryViewModel(workoutRepository, routineRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
