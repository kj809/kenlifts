package com.kenlifts.progression

import com.kenlifts.util.roundToNearest2_5

/**
 * StrongLifts-style progression rules per exercise.
 * Lower body: +5kg (Squat, Deadlift)
 * Upper body: +2.5kg (Bench Press, Overhead Press, Barbell Row)
 */
object ProgressionRules {

    private const val LOWER_INCREMENT_KG = 5f
    private const val UPPER_INCREMENT_KG = 2.5f
    private const val DELOAD_FACTOR = 0.9f
    private const val DELOAD_FAILURES = 3

    /** Exercise IDs from seed: 1=Squat, 2=Bench, 3=Row, 4=OHP, 5=Deadlift */
    private val LOWER_BODY_IDS = setOf(1L, 5L)  // Squat, Deadlift

    fun getIncrementKg(exerciseId: Long): Float =
        if (exerciseId in LOWER_BODY_IDS) LOWER_INCREMENT_KG else UPPER_INCREMENT_KG

    /** Deload by 10%, rounded to nearest 2.5kg. Minimum 2.5kg. */
    fun deloadedWeight(currentKg: Float): Float {
        val reduced = currentKg * DELOAD_FACTOR
        val rounded = roundToNearest2_5(reduced)
        return rounded.coerceAtLeast(2.5f)
    }

    val deloadFailuresThreshold: Int get() = DELOAD_FAILURES
}
