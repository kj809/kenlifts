package com.kenlifts.ui.navigation

object NavRoutes {
    const val Home = "home"
    const val EditRoutines = "edit_routines"
    const val WorkoutSession = "workout_session/{routineId}"
    const val Settings = "settings"
    const val History = "history"
    const val ProgressCharts = "progress_charts"
    const val WorkoutDetail = "workout_detail/{workoutId}"

    fun workoutSession(routineId: Long) = "workout_session/$routineId"
    fun workoutDetail(workoutId: Long) = "workout_detail/$workoutId"
}
