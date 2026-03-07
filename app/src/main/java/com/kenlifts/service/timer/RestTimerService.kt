package com.kenlifts.service.timer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.kenlifts.KenliftsApplication
import com.kenlifts.tile.qs.TileUpdateHelper
import com.kenlifts.widget.appwidget.WidgetUpdateHelper
import com.kenlifts.MainActivity
import com.kenlifts.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.kenlifts.data.ChimeMode
import com.kenlifts.data.Milestones
import com.kenlifts.data.PreferencesManager

class RestTimerService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var countdownJob: Job? = null

    private var totalSeconds: Int = 0
    private var remainingSeconds: Int = 0
    private val firedMilestones = mutableSetOf<Int>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                totalSeconds = intent.getIntExtra(EXTRA_TOTAL_SECONDS, 120)
                remainingSeconds = totalSeconds
                firedMilestones.clear()
                startForeground(NOTIFICATION_ID, createNotification())
                startCountdown()
            }
            ACTION_RESET -> {
                if (totalSeconds > 0) {
                    remainingSeconds = totalSeconds
                    firedMilestones.clear()
                    startCountdown()
                } else {
                    WidgetUpdateHelper.updateWidgets(applicationContext)
                    TileUpdateHelper.requestTileUpdate(applicationContext)
                }
            }
            ACTION_STOP -> {
                stopCountdown()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startCountdown() {
        countdownJob?.cancel()
                TimerManager.updateState(active = true, remainingSeconds = remainingSeconds, totalSeconds = totalSeconds)
        WidgetUpdateHelper.updateWidgets(applicationContext)
        TileUpdateHelper.requestTileUpdate(applicationContext)

        countdownJob = serviceScope.launch {
            while (remainingSeconds > 0 && isActive) {
                delay(1000L)
                remainingSeconds--
                TimerManager.updateState(active = true, remainingSeconds = remainingSeconds, totalSeconds = totalSeconds)
                updateNotification()
                WidgetUpdateHelper.updateWidgets(applicationContext)
                TileUpdateHelper.requestTileUpdate(applicationContext)
                fireMilestoneChimeIfNeeded()
            }
            if (remainingSeconds <= 0) {
                fireMilestoneChimeIfNeeded()
                TimerManager.updateState(active = false, remainingSeconds = 0, totalSeconds = totalSeconds)
                WidgetUpdateHelper.updateWidgets(applicationContext)
                TileUpdateHelper.requestTileUpdate(applicationContext)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        TimerManager.updateState(active = false, remainingSeconds = 0, totalSeconds = 0)
        WidgetUpdateHelper.updateWidgets(applicationContext)
        TileUpdateHelper.requestTileUpdate(applicationContext)
    }

    private fun fireMilestoneChimeIfNeeded() {
        val elapsed = totalSeconds - remainingSeconds
        val chimeMode = runBlocking {
            PreferencesManager(this@RestTimerService).chimeMode.first()
        }
        if (chimeMode == ChimeMode.NONE) return
        if (chimeMode == ChimeMode.START_ONLY) return

        Milestones.values.forEach { milestone ->
            if (elapsed >= milestone && milestone !in firedMilestones) {
                firedMilestones.add(milestone)
                chime()
            }
        }
    }

    private fun chime() {
        try {
            val prefs = PreferencesManager(this)
            val chimeMode = runBlocking { prefs.chimeMode.first() }
            if (chimeMode == ChimeMode.NONE) return

            if (chimeMode == ChimeMode.BOTH || chimeMode == ChimeMode.MILESTONE_ONLY) {
                val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(applicationContext, defaultSound)
                ringtone?.play()
            }
            if (chimeMode == ChimeMode.BOTH || chimeMode == ChimeMode.MILESTONE_ONLY) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    (getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    getSystemService(VIBRATOR_SERVICE) as? Vibrator
                }
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(300)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    private fun createNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val resetIntent = PendingIntent.getService(
            this, 1, Intent(this, RestTimerService::class.java).apply { action = ACTION_RESET },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 2, Intent(this, RestTimerService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, KenliftsApplication.NOTIFICATION_CHANNEL_REST_TIMER_ID)
            .setContentTitle(getString(R.string.rest_timer_notification_title))
            .setContentText(formatTime(remainingSeconds))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_play, getString(R.string.rest_timer_reset), resetIntent)
            .addAction(android.R.drawable.ic_delete, getString(R.string.rest_timer_stop), stopIntent)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopCountdown()
    }

    companion object {
        const val ACTION_START = "com.kenlifts.REST_TIMER_START"
        const val ACTION_RESET = "com.kenlifts.REST_TIMER_RESET"
        const val ACTION_STOP = "com.kenlifts.REST_TIMER_STOP"
        const val EXTRA_TOTAL_SECONDS = "total_seconds"
        private const val NOTIFICATION_ID = 1002
    }
}
