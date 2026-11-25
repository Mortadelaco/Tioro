package com.example.tioro

import io.github.mortadelaco.tioro.R
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.example.tioro.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TioroStreakWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        updateAllWidgets(context)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TioroStreakWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val dao = db.dailyRecordDao()

                // Calcular racha
                var streak = 0
                var offset = 0
                while (offset < 3650) {
                    val dateToCheck = LocalDate.now().minusDays(offset.toLong()).toString()
                    val r = dao.getRecordByDate(dateToCheck)
                    if (r != null && r.dailyScore >= 50.0f) {
                        streak++
                    } else {
                        if (offset != 0) break
                    }
                    offset++
                }

                val todayStr = LocalDate.now().toString()
                val todayRecord = dao.getRecordByDate(todayStr)
                val isSavedToday = todayRecord != null && todayRecord.dailyScore >= 50.0f

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                appWidgetIds.forEach { widgetId ->
                    val views = RemoteViews(context.packageName, R.layout.widget_tioro_streak).apply {
                        setTextViewText(R.id.widget_streak_number, "$streak")
                        setTextViewText(
                            R.id.widget_streak_label,
                            if (streak == 1) context.getString(R.string.streak_single_day)
                            else context.getString(R.string.streak_plural_days)
                        )
                        setTextViewText(
                            R.id.widget_streak_status,
                            if (isSavedToday) context.getString(R.string.widget_streak_today_saved)
                            else context.getString(R.string.widget_streak_today_unsaved)
                        )
                        setTextColor(
                            R.id.widget_streak_status,
                            if (isSavedToday) Color.parseColor("#E5C07B") else Color.parseColor("#A0A0A0")
                        )
                        setOnClickPendingIntent(R.id.widget_streak_container, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }
    }
}
