package com.kenlifts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenlifts.ui.components.SetCircle
import com.kenlifts.viewmodel.ExerciseSetDetail
import com.kenlifts.viewmodel.WorkoutDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    viewModel: WorkoutDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val detail by viewModel.detail.collectAsState(initial = null)

    LaunchedEffect(workoutId) {
        viewModel.loadWorkout(workoutId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.routineName ?: "Workout") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        val exercises = detail?.exerciseDetails ?: emptyList()
        if (exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                detail?.let { d ->
                    item {
                        Text(
                            text = dateFormat.format(Date(d.startedAt)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        d.completedAt?.let { completed ->
                            Text(
                                text = "Completed " + dateFormat.format(Date(completed)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                items(exercises) { exerciseDetail ->
                    ExerciseDetailCard(exerciseDetail = exerciseDetail)
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailCard(exerciseDetail: ExerciseSetDetail) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exerciseDetail.exerciseName,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                (0 until exerciseDetail.expectedSets).forEach { setIndex ->
                    val completedSet = exerciseDetail.completedSets[setIndex]
                    val completed = completedSet != null
                    SetCircle(
                        reps = completedSet?.repsCompleted ?: exerciseDetail.expectedReps,
                        completed = completed,
                        onClick = null,
                        weightKg = completedSet?.weightKg
                    )
                }
            }
        }
    }
}
