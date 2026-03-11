package com.kenlifts.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SetCircle(
    targetReps: Int,
    loggedReps: Int?,
    onClick: (() -> Unit)? = null,
    weightKg: Float? = null
) {
    val filled = loggedReps != null
    val displayReps = loggedReps ?: targetReps
    val backgroundColor = if (filled) {
        Color(0xFFE53935) // Red when logged (any reps)
    } else {
        Color(0xFF9E9E9E) // Gray when empty
    }
    val modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(backgroundColor)
        .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
        )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            if (filled) {
                Text(
                    text = displayReps.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp
                )
            } else {
                Text(
                    text = targetReps.toString(),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 18.sp
                )
            }
        }
        if (weightKg != null) {
            Text(
                text = "%.1f kg".format(weightKg),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
