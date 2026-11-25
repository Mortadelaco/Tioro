package com.example.tioro

import io.github.mortadelaco.tioro.R
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.tioro.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class TioroWidgetProvider : AppWidgetProvider() {

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
            val componentName = ComponentName(context, TioroWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isEmpty()) return

            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val dao = db.dailyRecordDao()

                val todayStr = LocalDate.now().toString()
                val record = dao.getRecordByDate(todayStr)
                val todayScore = record?.dailyScore ?: 0f

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

                // Objetivos del bloque de esta semana
                val currentMondayStr = LocalDate.now().with(DayOfWeek.MONDAY).toString()
                val objs = dao.getObjectivesForWeekSnapshot(currentMondayStr)
                val completedObjs = objs.count { it.isCompleted }
                val totalObjs = objs.size

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
                    val views = RemoteViews(context.packageName, R.layout.widget_tioro).apply {
                        val scoreFormatted = String.format(Locale.getDefault(), "%.1f", todayScore)
                        setTextViewText(
                            R.id.widget_score_text,
                            context.getString(R.string.widget_score_text_format, scoreFormatted)
                        )
                        setTextViewText(R.id.widget_streak_text, "🔥 $streak")
                        setTextViewText(
                            R.id.widget_block_text,
                            context.getString(R.string.widget_block_text_format, completedObjs, totalObjs)
                        )
                        setTextViewText(
                            R.id.widget_add_button,
                            context.getString(R.string.widget_btn_add_activity)
                        )
                        setOnClickPendingIntent(R.id.widget_container, pendingIntent)
                        setOnClickPendingIntent(R.id.widget_add_button, pendingIntent)
                    }
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }
    }
}
