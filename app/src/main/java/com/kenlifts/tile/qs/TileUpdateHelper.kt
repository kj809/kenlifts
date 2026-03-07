package com.kenlifts.tile.qs

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.quicksettings.TileService

/**
 * Requests the Rest Timer Quick Settings tile to refresh.
 * When the system rebinds the tile, onStartListening will run and display current timer state.
 */
object TileUpdateHelper {
    fun requestTileUpdate(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            TileService.requestListeningState(
                context,
                ComponentName(context, KenliftsTileService::class.java)
            )
        }
    }
}
