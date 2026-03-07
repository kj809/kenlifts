package com.kenlifts.util

import kotlin.math.roundToInt

/**
 * Rounds weight to the nearest 2.5 kg (e.g. 10.3 -> 10.0, 11.2 -> 10.0, 11.3 -> 12.5).
 */
fun roundToNearest2_5(weightKg: Float): Float {
    val steps = (weightKg / 2.5f).roundToInt()
    return (steps * 2.5f)
}
