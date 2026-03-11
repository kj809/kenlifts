package com.kenlifts.widget.appwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.kenlifts.MainActivity
import com.kenlifts.R
import com.kenlifts.service.timer.RestTimerService
import com.kenlifts.service.timer.TimerManager

class KenliftsAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            intent.action == ACTION_UPDATE_WIDGET
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(ComponentName(context, KenliftsAppWidget::class.java))
            for (id in ids) {
                updateAppWidget(context, appWidgetManager, id)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.app_widget)

        val timerState = TimerManager.state.value
        val timerActive = timerState.active && timerState.totalSeconds > 0

        views.setViewVisibility(
            R.id.widget_timer_section,
            if (timerActive) View.VISIBLE else View.GONE
        )
        if (timerActive) {
            val minutes = timerState.elapsedSeconds / 60
            val seconds = timerState.elapsedSeconds % 60
            views.setTextViewText(
                R.id.widget_timer_text,
                context.getString(R.string.rest_timer_notification_title) + ": " + "%d:%02d".format(minutes, seconds)
            )
            views.setOnClickPendingIntent(
                R.id.widget_btn_reset_timer,
                PendingIntent.getService(
                    context,
                    0,
                    Intent(context, RestTimerService::class.java).apply { action = RestTimerService.ACTION_RESET },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }

        views.setOnClickPendingIntent(
            R.id.widget_btn_routine_a,
            createOpenWorkoutPendingIntent(context, ROUTINE_A_ID)
        )
        views.setOnClickPendingIntent(
            R.id.widget_btn_routine_b,
            createOpenWorkoutPendingIntent(context, ROUTINE_B_ID)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun createOpenWorkoutPendingIntent(context: Context, routineId: Long): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(MainActivity.EXTRA_ROUTINE_ID, routineId)
        }
        return PendingIntent.getActivity(
            context,
            routineId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.kenlifts.ACTION_UPDATE_WIDGET"
        const val ROUTINE_A_ID = 1L
        const val ROUTINE_B_ID = 2L
    }
}
