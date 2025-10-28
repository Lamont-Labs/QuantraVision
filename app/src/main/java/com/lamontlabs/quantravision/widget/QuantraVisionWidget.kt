package com.lamontlabs.quantravision.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.lamontlabs.quantravision.R
import com.lamontlabs.quantravision.gamification.UserStats
import com.lamontlabs.quantravision.gamification.BonusHighlights

/**
 * QuantraVisionWidget
 * Home screen widget showing quick stats and patterns detected today
 */
class QuantraVisionWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val stats = UserStats.load(context)
        val bonusHighlights = BonusHighlights.available(context)

        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_quantravision)

        // Update text views
        views.setTextViewText(R.id.widget_streak, "${stats.currentStreak}")
        views.setTextViewText(R.id.widget_detections, "${stats.totalDetections}")
        views.setTextViewText(R.id.widget_bonus, "+$bonusHighlights")
        views.setTextViewText(R.id.widget_favorite, stats.favoritePattern.ifEmpty { "None" })

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, QuantraVisionWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            for (appWidgetId in appWidgetIds) {
                val widget = QuantraVisionWidget()
                widget.updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }
}
