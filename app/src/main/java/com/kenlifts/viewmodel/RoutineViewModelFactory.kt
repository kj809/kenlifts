package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kenlifts.repository.RoutineRepository

class RoutineViewModelFactory(private val routineRepository: RoutineRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutineViewModel::class.java)) {
            return RoutineViewModel(routineRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
