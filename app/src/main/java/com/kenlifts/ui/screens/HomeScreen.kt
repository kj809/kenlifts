package com.kenlifts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kenlifts.data.room.RoutineEntity
import com.kenlifts.viewmodel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: RoutineViewModel,
    onStartRoutine: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kenlifts") })
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                routines.filter { it.name == "Routine A" || it.name == "Routine B" }.forEach { routine ->
                    FilledTonalButton(
                        onClick = { onStartRoutine(routine.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Start ${routine.name.replace("Routine ", "")}")
                    }
                }
            }
        }
    }
}
