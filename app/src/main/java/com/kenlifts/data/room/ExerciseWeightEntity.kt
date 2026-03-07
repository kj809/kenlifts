package com.kenlifts.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercise_weights",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId", unique = true)]
)
data class ExerciseWeightEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseId: Long,
    val weightKg: Float?,
    /** Weight increment on success (kg). Squat/Deadlift 5.0, Bench/OHP/Row 2.5. */
    val incrementKg: Float? = null,
    /** Number of consecutive workouts failed at current working weight. Reset on success or deload. */
    val consecutiveFailures: Int = 0,
    /** Weight used in the last attempt of this exercise. */
    val lastAttemptWeightKg: Float? = null
)
