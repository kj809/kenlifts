package com.kenlifts.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kenlifts.data.ChimeMode
import com.kenlifts.data.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val preferencesManager: PreferencesManager) : ViewModel() {
    val chimeMode = preferencesManager.chimeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ChimeMode.BOTH
    )

    val bodyWeightKg = preferencesManager.bodyWeightKg.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun setChimeMode(mode: ChimeMode) {
        viewModelScope.launch {
            preferencesManager.setChimeMode(mode)
        }
    }

    fun setBodyWeightKg(weight: Float?) {
        viewModelScope.launch {
            preferencesManager.setBodyWeightKg(weight)
        }
    }
}
