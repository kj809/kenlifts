package com.kenlifts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kenlifts.data.room.WorkoutEntity
import com.kenlifts.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onWorkoutClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val workouts by viewModel.workoutsWithRoutineNames.collectAsState()
    val workoutDates by viewModel.workoutDates.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("History") })
        },
        modifier = modifier
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("List") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Calendar") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Notes") })
            }
            when (selectedTab) {
                0 -> HistoryList(workouts = workouts, onWorkoutClick = onWorkoutClick)
                1 -> HistoryCalendar(workoutDates = workoutDates, workouts = workouts, onWorkoutClick = onWorkoutClick)
                2 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Notes coming soon", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun HistoryList(
    workouts: List<com.kenlifts.viewmodel.WorkoutWithRoutine>,
    onWorkoutClick: (Long) -> Unit
) {
    if (workouts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No workouts yet", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workouts) { (workout, routineName) ->
                WorkoutHistoryCard(workout = workout, routineName = routineName, onClick = { onWorkoutClick(workout.id) })
            }
        }
    }
}

@Composable
private fun HistoryCalendar(
    workoutDates: Set<Long>,
    workouts: List<com.kenlifts.viewmodel.WorkoutWithRoutine>,
    onWorkoutClick: (Long) -> Unit
) {
    val calendar = remember { Calendar.getInstance() }
    var displayMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var displayYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
    val firstDay = remember(displayMonth, displayYear) { Calendar.getInstance().apply { set(displayYear, displayMonth, 1) } }
    val maxDay = firstDay.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startOffset = (firstDay.get(Calendar.DAY_OF_WEEK) - 1) % 7
    val weeks = ((startOffset + maxDay) + 6) / 7

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                if (displayMonth == 0) { displayMonth = 11; displayYear -= 1 }
                else displayMonth -= 1
            }) { Text("<") }
            Text("${monthNames[displayMonth]} $displayYear", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = {
                if (displayMonth == 11) { displayMonth = 0; displayYear += 1 }
                else displayMonth += 1
            }) { Text(">") }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            dayHeaders.forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
        }
        (0 until weeks).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                (0..6).forEach { col ->
                    val dayIndex = week * 7 + col
                    val day = dayIndex - startOffset + 1
                    if (day in 1..maxDay) {
                        val dayCal = Calendar.getInstance().apply {
                            set(displayYear, displayMonth, day, 0, 0, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val hasWorkout = dayCal.timeInMillis in workoutDates
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(
                                    if (hasWorkout) Modifier.background(MaterialTheme.colorScheme.primary)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (hasWorkout) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Box(Modifier.size(40.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: WorkoutEntity,
    routineName: String,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = routineName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = dateFormat.format(Date(workout.startedAt)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (workout.completedAt != null) {
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
