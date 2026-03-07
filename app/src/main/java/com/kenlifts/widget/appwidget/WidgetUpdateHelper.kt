package com.kenlifts.widget.appwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object WidgetUpdateHelper {
    fun updateWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, KenliftsAppWidget::class.java)
        val ids = appWidgetManager.getAppWidgetIds(component)
        if (ids.isNotEmpty()) {
            context.sendBroadcast(
                Intent(KenliftsAppWidget.ACTION_UPDATE_WIDGET)
                    .setPackage(context.packageName)
            )
        }
    }
}
