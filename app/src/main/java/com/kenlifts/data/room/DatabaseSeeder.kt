package com.kenlifts.data.room

import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseSeeder {

    fun seedSync(db: SupportSQLiteDatabase) {
        db.execSQL("INSERT OR IGNORE INTO exercises (id, name) VALUES (1, 'Squat'), (2, 'Bench Press'), (3, 'Barbell Row'), (4, 'Overhead Press'), (5, 'Deadlift')")
        db.execSQL("INSERT OR IGNORE INTO routines (id, name) VALUES (1, 'Routine A'), (2, 'Routine B')")
        db.execSQL("""
            INSERT OR IGNORE INTO routine_exercises (routineId, exerciseId, sets, reps, restSeconds, orderIndex) VALUES
            (1, 1, 5, 5, 120, 0),
            (1, 2, 5, 5, 120, 1),
            (1, 3, 5, 5, 120, 2),
            (2, 1, 5, 5, 120, 0),
            (2, 4, 5, 5, 120, 1),
            (2, 5, 1, 5, 120, 2)
        """.trimIndent())
        db.execSQL("""
            INSERT OR IGNORE INTO exercise_weights (exerciseId, weightKg, incrementKg, consecutiveFailures, lastAttemptWeightKg) VALUES
            (1, 20, 5.0, 0, NULL),
            (2, 20, 2.5, 0, NULL),
            (3, 20, 2.5, 0, NULL),
            (4, 20, 2.5, 0, NULL),
            (5, 20, 5.0, 0, NULL)
        """.trimIndent())
    }
}
