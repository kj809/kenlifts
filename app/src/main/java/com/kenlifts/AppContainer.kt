package com.kenlifts

import android.app.Application
import android.content.Context
import com.kenlifts.data.PreferencesManager
import com.kenlifts.data.room.KenliftsDatabase
import com.kenlifts.repository.ExerciseRepository
import com.kenlifts.repository.ExerciseWeightRepository
import com.kenlifts.repository.RoutineExerciseRepository
import com.kenlifts.repository.RoutineRepository
import com.kenlifts.repository.WorkoutSetRepository
import com.kenlifts.repository.WorkoutRepository
import com.kenlifts.progression.ProgressionService

class AppContainer(context: Context) {
    val application: Application = context.applicationContext as Application
    private val database = KenliftsDatabase.create(context)
    val preferencesManager = PreferencesManager(context)

    val routineRepository = RoutineRepository(database.routineDao())
    val routineExerciseRepository = RoutineExerciseRepository(database.routineExerciseDao())
    val exerciseRepository = ExerciseRepository(database.exerciseDao())
    val exerciseWeightRepository = ExerciseWeightRepository(database.exerciseWeightDao())
    val workoutRepository = WorkoutRepository(database.workoutDao())
    val workoutSetRepository = WorkoutSetRepository(database.workoutSetDao())
    val progressionService = ProgressionService(
        workoutSetRepository,
        routineExerciseRepository,
        exerciseWeightRepository
    )
}
