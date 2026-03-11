package com.kenlifts.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ChimeMode(val value: String) {
    BOTH("BOTH"),
    START_ONLY("START_ONLY"),
    MILESTONE_ONLY("MILESTONE_ONLY"),
    NONE("NONE");

    companion object {
        fun from(value: String?) = entries.find { it.value == value } ?: BOTH
    }
}

object Milestones {
    val values: List<Int> = listOf(60, 90, 120)
}

class PreferencesManager(private val context: Context) {
    private object Keys {
        val lastSyncTime = longPreferencesKey("last_sync_time")
        val chimeMode = stringPreferencesKey("chime_mode")
        val bodyWeightKg = floatPreferencesKey("body_weight_kg")
    }

    val lastSyncTime: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.lastSyncTime] ?: 0L
    }

    val chimeMode: Flow<ChimeMode> = context.dataStore.data.map { prefs ->
        ChimeMode.from(prefs[Keys.chimeMode])
    }

    val bodyWeightKg: Flow<Float?> = context.dataStore.data.map { prefs ->
        prefs[Keys.bodyWeightKg]
    }

    suspend fun setLastSyncTime(time: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.lastSyncTime] = time
        }
    }

    suspend fun setChimeMode(mode: ChimeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.chimeMode] = mode.value
        }
    }

    suspend fun setBodyWeightKg(weight: Float?) {
        context.dataStore.edit { prefs ->
            if (weight != null) prefs[Keys.bodyWeightKg] = weight else prefs.remove(Keys.bodyWeightKg)
        }
    }
}
