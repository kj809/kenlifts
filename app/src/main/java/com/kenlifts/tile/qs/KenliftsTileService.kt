package com.kenlifts.tile.qs

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.kenlifts.R
import com.kenlifts.service.timer.TimerManager
import kotlinx.coroutines.runBlocking

/**
 * Quick Settings tile for the Rest Timer.
 * Tap when inactive: start 120s rest timer.
 * Tap when active: reset timer to 120s.
 * Subtitle shows remaining time (e.g. "1:45") when active.
 */
class KenliftsTileService : TileService() {

    companion object {
        private const val DEFAULT_REST_SECONDS = 120
    }

    override fun onStartListening() {
        super.onStartListening()
        refreshTile()
    }

    override fun onClick() {
        super.onClick()
        val state = runBlocking { TimerManager.state.value }
        if (state.active && state.remainingSeconds > 0) {
            TimerManager.reset(this)
        } else {
            TimerManager.start(this, DEFAULT_REST_SECONDS)
        }
        refreshTile()
    }

    private fun refreshTile() {
        val state = runBlocking { TimerManager.state.value }
        val active = state.active && state.remainingSeconds > 0
        qsTile?.apply {
            label = getString(R.string.qs_tile_rest_timer)
            icon = Icon.createWithResource(this@KenliftsTileService, android.R.drawable.ic_media_play)
            this.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                subtitle = if (active) formatTime(state.remainingSeconds) else null
            }
            updateTile()
        }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }
}
