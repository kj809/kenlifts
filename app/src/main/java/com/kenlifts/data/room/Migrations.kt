package com.kenlifts.data.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS lifts")

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS routines (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS routine_exercises (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                routineId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                sets INTEGER NOT NULL,
                reps INTEGER NOT NULL,
                restSeconds INTEGER NOT NULL,
                orderIndex INTEGER NOT NULL,
                FOREIGN KEY(routineId) REFERENCES routines(id) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_routine_exercises_routineId ON routine_exercises(routineId)
        """.trimIndent())
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_routine_exercises_exerciseId ON routine_exercises(exerciseId)
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS exercise_weights (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                exerciseId INTEGER NOT NULL,
                weightKg REAL,
                FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE UNIQUE INDEX IF NOT EXISTS index_exercise_weights_exerciseId ON exercise_weights(exerciseId)
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workouts (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                routineId INTEGER NOT NULL,
                startedAt INTEGER NOT NULL,
                completedAt INTEGER,
                FOREIGN KEY(routineId) REFERENCES routines(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workouts_routineId ON workouts(routineId)
        """.trimIndent())
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workouts_startedAt ON workouts(startedAt)
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workout_sets (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                workoutId INTEGER NOT NULL,
                exerciseId INTEGER NOT NULL,
                routineExerciseId INTEGER,
                setIndex INTEGER NOT NULL,
                repsCompleted INTEGER,
                weightKg REAL,
                FOREIGN KEY(workoutId) REFERENCES workouts(id) ON DELETE CASCADE,
                FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE,
                FOREIGN KEY(routineExerciseId) REFERENCES routine_exercises(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workout_sets_workoutId ON workout_sets(workoutId)
        """.trimIndent())
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workout_sets_exerciseId ON workout_sets(exerciseId)
        """.trimIndent())
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workout_sets_routineExerciseId ON workout_sets(routineExerciseId)
        """.trimIndent())

        DatabaseSeeder.seedSync(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_weights ADD COLUMN consecutiveFailures INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE exercise_weights ADD COLUMN incrementKg REAL")
        db.execSQL("ALTER TABLE exercise_weights ADD COLUMN lastAttemptWeightKg REAL")
        // Seed increments for existing exercises: 1=Squat 5, 2=Bench 2.5, 3=Row 2.5, 4=OHP 2.5, 5=Deadlift 5
        db.execSQL("UPDATE exercise_weights SET incrementKg = 5.0 WHERE exerciseId = 1")
        db.execSQL("UPDATE exercise_weights SET incrementKg = 2.5 WHERE exerciseId IN (2, 3, 4)")
        db.execSQL("UPDATE exercise_weights SET incrementKg = 5.0 WHERE exerciseId = 5")
    }
}
