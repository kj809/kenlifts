package com.kenlifts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.kenlifts.service.timer.TimerManager
import com.kenlifts.ui.components.SetCircle
import com.kenlifts.viewmodel.WorkoutExerciseItem
import com.kenlifts.viewmodel.WorkoutSessionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    routineId: Long,
    viewModel: WorkoutSessionViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val timerState by TimerManager.state.collectAsState()

    LaunchedEffect(routineId) {
        viewModel.startWorkout(routineId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.routineName ?: "Workout") },
                navigationIcon = {
                    TextButton(onClick = {
                        viewModel.completeWorkout()
                        onBack()
                    }) {
                        Text("End")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        if (state.exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val currentExercise = state.exercises.getOrNull(state.currentExerciseIndex)
            if (currentExercise == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Workout complete!")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp)
                ) {
                    if (timerState.active && timerState.remainingSeconds > 0) {
                        RestTimerBar(
                            secondsRemaining = timerState.remainingSeconds,
                            onReset = { viewModel.resetRestTimer() },
                            onStop = { viewModel.stopRestTimer() }
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                    ExerciseBlock(
                        exercise = currentExercise,
                        onSetClick = { setIndex ->
                            viewModel.toggleSetCompleted(currentExercise.exerciseId, setIndex)
                        },
                        onWeightChange = { viewModel.updateWeight(currentExercise.exerciseId, it) },
                        isSetCompleted = { viewModel.isSetCompleted(currentExercise.exerciseId, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RestTimerBar(
    secondsRemaining: Int,
    onReset: () -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val minutes = secondsRemaining / 60
    val secs = secondsRemaining % 60
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Rest: %d:%02d".format(minutes, secs),
                style = MaterialTheme.typography.titleLarge
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReset) { Text("Reset") }
                TextButton(onClick = onStop) { Text("Stop") }
            }
        }
    }
}

@Composable
private fun ExerciseBlock(
    exercise: WorkoutExerciseItem,
    onSetClick: (Int) -> Unit,
    onWeightChange: (Float?) -> Unit,
    isSetCompleted: (Int) -> Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleLarge
                )
                if (exercise.consecutiveFailures > 0) {
                    Text(
                        text = "Fail: ${exercise.consecutiveFailures}/3",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (exercise.nextWeightPreview != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = exercise.nextWeightPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0 until exercise.sets).forEach { setIndex ->
                    SetCircle(
                        reps = exercise.reps,
                        completed = isSetCompleted(setIndex),
                        onClick = { onSetClick(setIndex) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            WeightEditor(
                currentWeight = exercise.weightKg,
                onWeightChange = onWeightChange
            )
        }
    }
}

@Composable
private fun WeightEditor(
    currentWeight: Float?,
    onWeightChange: (Float?) -> Unit
) {
    var text by remember(currentWeight) {
        mutableStateOf(currentWeight?.toString() ?: "")
    }
    var isEditing by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Weight (kg):",
            style = MaterialTheme.typography.bodyMedium
        )
        if (isEditing) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                singleLine = true,
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )
            TextButton(onClick = {
                val parsed = text.toFloatOrNull()
                onWeightChange(parsed)
                isEditing = false
                text = parsed?.toString() ?: ""
            }) {
                Text("Save")
            }
            TextButton(onClick = {
                isEditing = false
                text = currentWeight?.toString() ?: ""
            }) {
                Text("Cancel")
            }
        } else {
            Text(
                text = currentWeight?.let { "%.1f".format(it) } ?: "—",
                style = MaterialTheme.typography.bodyLarge
            )
            TextButton(onClick = { isEditing = true }) {
                Text("Edit")
            }
        }
    }
}
