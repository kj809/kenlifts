package com.kenlifts.ui.screens

import android.graphics.Color
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.kenlifts.viewmodel.ExerciseChartData
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
            Text(
                text = "Select exercises",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            ExerciseFilterChips(
                exercises = state.exercises,
                selectedIds = state.selectedExerciseIds,
                onToggle = { viewModel.toggleExerciseSelection(it) }
            )
            Spacer(Modifier.height(16.dp))
            RangeFilterChips(
                selectedRange = state.range,
                onRangeSelect = viewModel::setRange
            )
            Spacer(Modifier.height(16.dp))
            ProgressLineChart(
                chartDataByExercise = state.chartDataByExercise,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }
    }
}

@Composable
private fun ExerciseFilterChips(
    exercises: List<ExerciseEntity>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        exercises.chunked(3).forEach { rowExercises ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowExercises.forEach { exercise ->
                    FilterChip(
                        selected = exercise.id in selectedIds,
                        onClick = { onToggle(exercise.id) },
                        label = { Text(exercise.name) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

private val CHART_COLORS = listOf(
    "#6750A4", "#E53935", "#43A047", "#1E88E5", "#FB8C00"
)

@Composable
private fun ProgressLineChart(
    chartDataByExercise: List<ExerciseChartData>,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val allDates = remember(chartDataByExercise) {
        chartDataByExercise
            .flatMap { it.data.map { p -> p.startedAt } }
            .distinct()
            .sorted()
    }
    val dateToIndex = remember(allDates) {
        allDates.mapIndexed { i, d -> d to i }.toMap()
    }

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
            if (chartDataByExercise.isEmpty() || allDates.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }
            val lineData = LineData()
            chartDataByExercise.forEachIndexed { idx, exerciseData ->
                val color = Color.parseColor(CHART_COLORS[idx % CHART_COLORS.size])
                val entries = exerciseData.data
                    .mapNotNull { point ->
                        dateToIndex[point.startedAt]?.let { xIdx ->
                            point.maxWeightKg?.let { weight -> Entry(xIdx.toFloat(), weight) }
                        }
                    }
                if (entries.isNotEmpty()) {
                    val dataSet = LineDataSet(entries, exerciseData.exerciseName).apply {
                        setColor(color)
                        setCircleColor(color)
                        lineWidth = 2f
                        setDrawValues(true)
                        mode = LineDataSet.Mode.LINEAR
                    }
                    lineData.addDataSet(dataSet)
                }
            }
            chart.data = lineData
            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val idx = value.toInt().coerceIn(0, allDates.lastIndex)
                    return dateFormat.format(Date(allDates[idx]))
                }
            }
            chart.xAxis.granularity = 1f
            chart.legend.isEnabled = true
            chart.invalidate()
        },
        modifier = modifier
    )
}
