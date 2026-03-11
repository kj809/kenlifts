package com.kenlifts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (timerState.active && timerState.totalSeconds > 0) {
                    RestTimerBar(
                        elapsedSeconds = timerState.elapsedSeconds,
                        totalSeconds = timerState.totalSeconds,
                        onReset = { viewModel.resetRestTimer() },
                        onStop = { viewModel.stopRestTimer() }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.exercises, key = { it.exerciseId }) { exercise ->
                        val isCurrent = exercise == state.exercises.getOrNull(state.currentExerciseIndex)
                        CompactExerciseBlock(
                            exercise = exercise,
                            isCurrent = isCurrent,
                            onSetClick = { setIndex ->
                                viewModel.cycleSetReps(exercise.exerciseId, setIndex)
                            },
                            onWeightChange = { viewModel.updateWeight(exercise.exerciseId, it) },
                            getSetReps = { viewModel.getSetReps(exercise.exerciseId, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RestTimerBar(
    elapsedSeconds: Int,
    totalSeconds: Int,
    onReset: () -> Unit = {},
    onStop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val minutes = elapsedSeconds / 60
    val secs = elapsedSeconds % 60
    val progress = if (totalSeconds > 0) elapsedSeconds.toFloat() / totalSeconds else 0f
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "%d:%02d".format(minutes, secs),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Rest %dmin".format(totalSeconds / 60),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onReset) { Text("Reset") }
                    TextButton(onClick = onStop) { Text("Stop") }
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun CompactExerciseBlock(
    exercise: WorkoutExerciseItem,
    isCurrent: Boolean,
    onSetClick: (Int) -> Unit,
    onWeightChange: (Float?) -> Unit,
    getSetReps: (Int) -> Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.exerciseName,
                    style = MaterialTheme.typography.titleMedium
                )
                if (exercise.consecutiveFailures > 0) {
                    Text(
                        text = "Fail: ${exercise.consecutiveFailures}/3",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (exercise.nextWeightPreview != null) {
                Text(
                    text = exercise.nextWeightPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0 until exercise.sets).forEach { setIndex ->
                    SetCircle(
                        targetReps = exercise.reps,
                        loggedReps = getSetReps(setIndex),
                        onClick = { onSetClick(setIndex) }
                    )
                }
                Spacer(Modifier.weight(1f))
                CompactWeightEditor(
                    currentWeight = exercise.weightKg,
                    onWeightChange = onWeightChange
                )
            }
        }
    }
}

@Composable
private fun CompactWeightEditor(
    currentWeight: Float?,
    onWeightChange: (Float?) -> Unit
) {
    var text by remember(currentWeight) {
        mutableStateOf(currentWeight?.toString() ?: "")
    }
    var isEditing by remember { mutableStateOf(false) }

    if (isEditing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                singleLine = true,
                modifier = Modifier.width(60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal)
            )
            TextButton(onClick = {
                val parsed = text.toFloatOrNull()
                onWeightChange(parsed)
                isEditing = false
                text = parsed?.toString() ?: ""
            }) { Text("✓") }
            TextButton(onClick = {
                isEditing = false
                text = currentWeight?.toString() ?: ""
            }) { Text("✕") }
        }
    } else {
        TextButton(onClick = { isEditing = true }) {
            Text(
                text = currentWeight?.let { "%.1f kg".format(it) } ?: "— kg",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

