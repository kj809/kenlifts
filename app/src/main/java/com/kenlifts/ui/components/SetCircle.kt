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
    reps: Int,
    completed: Boolean,
    onClick: (() -> Unit)? = null,
    weightKg: Float? = null
) {
    val backgroundColor = if (completed) {
        Color(0xFF4CAF50)
    } else {
        Color(0xFFE53935)
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
            Text(
                text = reps.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp
            )
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
