package com.kenlifts.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kenlifts.AppContainer
import com.kenlifts.PendingNavigation
import com.kenlifts.viewmodel.EditRoutinesViewModel
import com.kenlifts.viewmodel.EditRoutinesViewModelFactory
import com.kenlifts.viewmodel.HistoryViewModel
import com.kenlifts.viewmodel.HistoryViewModelFactory
import com.kenlifts.viewmodel.ProgressChartsViewModel
import com.kenlifts.viewmodel.ProgressChartsViewModelFactory
import com.kenlifts.viewmodel.RoutineViewModel
import com.kenlifts.viewmodel.RoutineViewModelFactory
import com.kenlifts.viewmodel.SettingsViewModel
import com.kenlifts.viewmodel.SettingsViewModelFactory
import com.kenlifts.viewmodel.WorkoutDetailViewModel
import com.kenlifts.viewmodel.WorkoutDetailViewModelFactory
import com.kenlifts.viewmodel.WorkoutSessionViewModel
import com.kenlifts.viewmodel.WorkoutSessionViewModelFactory
import com.kenlifts.ui.navigation.NavRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val pendingRoutineId = PendingNavigation.routineId
    LaunchedEffect(pendingRoutineId) {
        pendingRoutineId?.let { id ->
            navController.navigate(NavRoutes.workoutSession(id)) {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
            PendingNavigation.consumeRoutineId()
        }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestinations = setOf(
        NavRoutes.Home,
        NavRoutes.EditRoutines,
        NavRoutes.History,
        NavRoutes.ProgressCharts,
        NavRoutes.Settings
    )
    val showBottomBar = currentDestination?.route in bottomBarDestinations

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    listOf(
                        Triple(NavRoutes.Home, "Home", Icons.Default.Home),
                        Triple(NavRoutes.EditRoutines, "Routines", Icons.Default.Edit),
                        Triple(NavRoutes.History, "History", Icons.Default.History),
                        Triple(NavRoutes.ProgressCharts, "Charts", Icons.Default.BarChart),
                        Triple(NavRoutes.Settings, "Settings", Icons.Default.Settings)
                    ).forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = null) },
                            label = { Text(label) },
                            selected = currentDestination?.route == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.Home) {
                val viewModel: RoutineViewModel = viewModel(
                    factory = RoutineViewModelFactory(appContainer.routineRepository)
                )
                HomeScreen(
                    viewModel = viewModel,
                    onStartRoutine = { routineId ->
                        navController.navigate(NavRoutes.workoutSession(routineId))
                    }
                )
            }
            composable(NavRoutes.EditRoutines) {
                val viewModel: EditRoutinesViewModel = viewModel(
                    factory = EditRoutinesViewModelFactory(
                        appContainer.routineRepository,
                        appContainer.routineExerciseRepository,
                        appContainer.exerciseRepository
                    )
                )
                EditRoutinesScreen(viewModel = viewModel)
            }
            composable(
                route = NavRoutes.WorkoutSession,
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: return@composable
                val viewModel: WorkoutSessionViewModel = viewModel(
                    factory = WorkoutSessionViewModelFactory(
                        appContainer.application,
                        appContainer.routineRepository,
                        appContainer.routineExerciseRepository,
                        appContainer.exerciseRepository,
                        appContainer.exerciseWeightRepository,
                        appContainer.workoutRepository,
                        appContainer.workoutSetRepository,
                        appContainer.progressionService
                    )
                )
                WorkoutSessionScreen(
                    routineId = routineId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(NavRoutes.Settings) {
                val viewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModelFactory(appContainer.preferencesManager)
                )
                SettingsScreen(viewModel = viewModel)
            }
            composable(NavRoutes.History) {
                val viewModel: HistoryViewModel = viewModel(
                    factory = HistoryViewModelFactory(
                        appContainer.workoutRepository,
                        appContainer.routineRepository
                    )
                )
                HistoryScreen(
                    viewModel = viewModel,
                    onWorkoutClick = { workoutId ->
                        navController.navigate(NavRoutes.workoutDetail(workoutId))
                    }
                )
            }
            composable(NavRoutes.ProgressCharts) {
                val viewModel: ProgressChartsViewModel = viewModel(
                    factory = ProgressChartsViewModelFactory(
                        appContainer.exerciseRepository,
                        appContainer.workoutSetRepository
                    )
                )
                ProgressChartsScreen(viewModel = viewModel)
            }
            composable(
                route = NavRoutes.WorkoutDetail,
                arguments = listOf(navArgument("workoutId") { type = NavType.LongType })
            ) { backStackEntry ->
                val workoutId = backStackEntry.arguments?.getLong("workoutId") ?: return@composable
                val viewModel: WorkoutDetailViewModel = viewModel(
                    factory = WorkoutDetailViewModelFactory(
                        appContainer.workoutRepository,
                        appContainer.workoutSetRepository,
                        appContainer.routineRepository,
                        appContainer.routineExerciseRepository,
                        appContainer.exerciseRepository
                    )
                )
                WorkoutDetailScreen(
                    workoutId = workoutId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
