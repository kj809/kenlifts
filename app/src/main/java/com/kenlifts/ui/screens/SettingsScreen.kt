package com.kenlifts.ui.screens

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenlifts.data.ChimeMode
import com.kenlifts.data.Milestones
import com.kenlifts.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val chimeMode by viewModel.chimeMode.collectAsState()
    val bodyWeightKg by viewModel.bodyWeightKg.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Chime mode",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChimeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = chimeMode == mode,
                        onClick = { viewModel.setChimeMode(mode) },
                        label = { Text(mode.value) }
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Milestones (seconds)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = Milestones.values.joinToString(", "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Body weight (kg)",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            BodyWeightField(
                value = bodyWeightKg,
                onValueChange = { viewModel.setBodyWeightKg(it) }
            )
        }
    }
}

@Composable
private fun BodyWeightField(
    value: Float?,
    onValueChange: (Float?) -> Unit
) {
    var text by remember { mutableStateOf(value?.toString() ?: "") }
    var isEditing by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (!isEditing) text = value?.toString() ?: ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isEditing) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                singleLine = true,
                label = { Text("kg") },
                modifier = Modifier.widthIn(min = 100.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            TextButton(onClick = {
                val parsed = text.toFloatOrNull()?.takeIf { it > 0f }
                onValueChange(parsed)
                isEditing = false
                text = parsed?.toString() ?: ""
            }) { Text("Save") }
            TextButton(onClick = {
                isEditing = false
                text = value?.toString() ?: ""
            }) { Text("Cancel") }
        } else {
            Text(
                text = value?.let { "%.1f kg".format(it) } ?: "Not set",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { isEditing = true }) { Text("Edit") }
        }
    }
}
