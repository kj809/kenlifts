package com.kenlifts

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class KenliftsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timerChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_TIMER_ID,
                getString(R.string.notification_channel_timer_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_timer_description)
                setShowBadge(false)
            }
            val restTimerChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_REST_TIMER_ID,
                getString(R.string.notification_channel_rest_timer_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_rest_timer_description)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(restTimerChannel)
        }
    }

    companion object {
        const val NOTIFICATION_CHANNEL_TIMER_ID = "timer_channel"
        const val NOTIFICATION_CHANNEL_REST_TIMER_ID = "rest_timer_channel"
    }
}
