package com.kenlifts.service.timer

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RestTimerState(
    val active: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0
)

object TimerManager {
    private val _state = MutableStateFlow(RestTimerState())
    val state: StateFlow<RestTimerState> = _state.asStateFlow()

    fun start(context: Context, totalSeconds: Int) {
        if (totalSeconds <= 0) return
        context.startForegroundService(
            Intent(context, RestTimerService::class.java).apply {
                action = RestTimerService.ACTION_START
                putExtra(RestTimerService.EXTRA_TOTAL_SECONDS, totalSeconds)
            }
        )
    }

    fun reset(context: Context) {
        context.startService(
            Intent(context, RestTimerService::class.java).apply {
                action = RestTimerService.ACTION_RESET
            }
        )
    }

    fun stop(context: Context) {
        context.startService(
            Intent(context, RestTimerService::class.java).apply {
                action = RestTimerService.ACTION_STOP
            }
        )
    }

    fun updateState(active: Boolean, remainingSeconds: Int, totalSeconds: Int) {
        _state.value = RestTimerState(
            active = active,
            remainingSeconds = remainingSeconds,
            totalSeconds = totalSeconds
        )
    }
}
