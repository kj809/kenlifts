package com.kenlifts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenlifts.data.room.ExerciseEntity
import com.kenlifts.data.room.RoutineEntity
import com.kenlifts.data.room.RoutineExerciseEntity
import com.kenlifts.viewmodel.EditRoutinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRoutinesScreen(
    viewModel: EditRoutinesViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val selectedRoutineId by viewModel.selectedRoutineId.collectAsState()
    val routineExercises by viewModel.routineExercises.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()

    LaunchedEffect(routines) {
        if (routines.isNotEmpty() && selectedRoutineId == null) {
            viewModel.selectRoutine(routines.first().id)
        }
    }
    LaunchedEffect(selectedRoutineId) {
        selectedRoutineId?.let { viewModel.loadRoutineExercises(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Routines") })
        },
        modifier = modifier
    ) { padding ->
        if (routines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val routine = selectedRoutineId?.let { id ->
                routines.find { it.id == id }
            } ?: routines.first()
            val currentId = selectedRoutineId ?: routine.id

            Column(modifier = Modifier.padding(padding)) {
                TabRow(selectedTabIndex = routines.indexOfFirst { it.id == currentId }) {
                    routines.forEach { r ->
                        Tab(
                            selected = r.id == currentId,
                            onClick = { viewModel.selectRoutine(r.id) },
                            text = { Text(r.name) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Exercises",
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalButton(
                        onClick = { viewModel.showAddExerciseDialog() }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add")
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(routineExercises) { re ->
                        RoutineExerciseRow(
                            routineExercise = re,
                            exerciseName = viewModel.getExerciseName(re.exerciseId),
                            isDeadlift = viewModel.isDeadlift(re.exerciseId),
                            onSetsChange = { if (!viewModel.isDeadlift(re.exerciseId)) viewModel.updateSets(re, it) },
                            onRepsChange = { viewModel.updateReps(re, it) },
                            onRestChange = { viewModel.updateRest(re, it) },
                            onDelete = { viewModel.deleteRoutineExercise(re) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog && availableExercises.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissAddDialog() },
            title = { Text("Add Exercise") },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(availableExercises) { ex ->
                        TextButton(
                            onClick = {
                                viewModel.addExerciseToRoutine(ex)
                                viewModel.dismissAddDialog()
                            }
                        ) {
                            Text(ex.name)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissAddDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun RoutineExerciseRow(
    routineExercise: RoutineExerciseEntity,
    exerciseName: String,
    isDeadlift: Boolean,
    onSetsChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit,
    onRestChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var sets by remember(routineExercise.id) { mutableIntStateOf(routineExercise.sets) }
    var reps by remember(routineExercise.id) { mutableIntStateOf(routineExercise.reps) }
    var rest by remember(routineExercise.id) { mutableIntStateOf(routineExercise.restSeconds) }
    LaunchedEffect(routineExercise) {
        sets = routineExercise.sets
        reps = routineExercise.reps
        rest = routineExercise.restSeconds
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exerciseName,
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isDeadlift) {
                        NumberField("Sets", sets, { sets = it; onSetsChange(it) })
                    } else {
                        Text("1×", style = MaterialTheme.typography.bodyMedium)
                    }
                    NumberField("Reps", reps, { reps = it; onRepsChange(it) })
                    NumberField("Rest(s)", rest, { rest = it; onRestChange(it) })
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { it.toIntOrNull()?.takeIf { n -> n in 1..999 }?.let { n -> onValueChange(n) } },
        label = { Text(label) },
        modifier = Modifier.width(70.dp),
        singleLine = true
    )
}
