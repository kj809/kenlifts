package com.kenlifts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object PendingNavigation {
    var routineId: Long? by mutableStateOf(null)
        private set

    fun setRoutineId(id: Long?) {
        routineId = id
    }

    fun consumeRoutineId(): Long? {
        val id = routineId
        routineId = null
        return id
    }
}
