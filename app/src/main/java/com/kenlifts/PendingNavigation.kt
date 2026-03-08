package com.kenlifts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object PendingNavigation {
    private var routineIdState by mutableStateOf<Long?>(null)

    val routineId: Long?
        get() = routineIdState

    fun setRoutineId(id: Long?) {
        routineIdState = id
    }

    fun consumeRoutineId(): Long? {
        val id = routineIdState
        routineIdState = null
        return id
    }
}
