# Kenlifts

Android workout tracker built with Kotlin, Jetpack Compose, and Material 3. StrongLifts-style progression with routines, rest timer, widgets, and Quick Settings tile.

## Setup

1. Open the project in Android Studio (Hedgehog or later recommended).
2. Sync Gradle and build.
3. Build and run on an emulator or device (API 26+).

## Features

- **Routines** – Routine A (Squat, Bench Press, Barbell Row) and Routine B (Squat, Overhead Press, Deadlift)
- **StrongLifts-style progression** – Automatic weight increments (+5kg lower body, +2.5kg upper), deload on 3 consecutive failures
- **Rest timer** – 120s countdown with notification, Quick Settings tile
- **App widget** – Start routines from home screen, reset timer when active
- **Quick Settings tile** – Rest Timer: tap to start/reset 120s timer
- **History & progress charts** – View past workouts and weight over time

## Structure

- **data/room** – Room entities, DAOs, database, migrations
- **data** – PreferencesManager (DataStore)
- **repository** – Routine, Exercise, Workout, WorkoutSet, ExerciseWeight repositories
- **progression** – ProgressionRules, ProgressionService (StrongLifts logic)
- **util** – WeightUtils (roundToNearest2_5)
- **ui/screens** – Compose screens (Home, WorkoutSession, History, ProgressCharts, Settings)
- **ui/components** – SetCircle, RoutineCard
- **viewmodel** – ViewModels for each screen
- **service/timer** – RestTimerService (foreground), TimerManager
- **widget/appwidget** – KenliftsAppWidget, WidgetUpdateHelper
- **tile/qs** – KenliftsTileService, TileUpdateHelper

## Permissions

- `POST_NOTIFICATIONS` – rest timer notification
- `VIBRATE` – milestone chimes
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` – RestTimerService
