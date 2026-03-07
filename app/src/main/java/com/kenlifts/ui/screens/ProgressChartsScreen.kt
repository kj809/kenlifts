package com.kenlifts.ui.screens

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.kenlifts.data.room.ExerciseEntity
import com.kenlifts.data.room.WorkoutWeightPoint
import com.kenlifts.viewmodel.ChartRange
import com.kenlifts.viewmodel.ProgressChartsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressChartsScreen(
    viewModel: ProgressChartsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Progress Charts") })
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ExerciseDropdown(
                exercises = state.exercises,
                selectedExercise = state.exercises.find { it.id == state.selectedExerciseId },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onExerciseSelect = {
                    viewModel.selectExercise(it.id)
                    expanded = false
                }
            )
            Spacer(Modifier.height(16.dp))
            RangeFilterChips(
                selectedRange = state.range,
                onRangeSelect = viewModel::setRange
            )
            Spacer(Modifier.height(16.dp))
            ProgressLineChart(
                data = state.chartData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExerciseDropdown(
    exercises: List<ExerciseEntity>,
    selectedExercise: ExerciseEntity?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onExerciseSelect: (ExerciseEntity) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = selectedExercise?.name ?: "Select exercise",
            onValueChange = {},
            readOnly = true,
            label = { Text("Exercise") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            exercises.forEach { exercise ->
                DropdownMenuItem(
                    text = { Text(exercise.name) },
                    onClick = { onExerciseSelect(exercise) }
                )
            }
        }
    }
}

@Composable
private fun RangeFilterChips(
    selectedRange: ChartRange,
    onRangeSelect: (ChartRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            ChartRange.ONE_MONTH to "1m",
            ChartRange.THREE_MONTHS to "3m",
            ChartRange.SIX_MONTHS to "6m",
            ChartRange.ALL to "All"
        ).forEach { (range, label) ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelect(range) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun ProgressLineChart(
    data: List<WorkoutWeightPoint>,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            val entries = data
                .mapIndexed { index, point ->
                    point.maxWeightKg?.let { weight ->
                        Entry(index.toFloat(), weight)
                    }
                }
                .filterNotNull()
            if (entries.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }
            val dataSet = LineDataSet(entries, "Weight (kg)").apply {
                color = Color.parseColor("#6750A4")
                setCircleColor(Color.parseColor("#6750A4"))
                lineWidth = 2f
                setDrawValues(true)
                mode = LineDataSet.Mode.LINEAR
            }
            chart.data = LineData(dataSet)
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt().coerceIn(0, data.lastIndex)
                    return dateFormat.format(Date(data[idx].startedAt))
                }
            }
            chart.xAxis.granularity = 1f
            chart.invalidate()
        },
        modifier = modifier
    )
}
