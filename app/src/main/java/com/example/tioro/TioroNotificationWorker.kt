package com.example.tioro

import io.github.mortadelaco.tioro.R
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tioro.data.local.AppDatabase
import com.example.tioro.data.local.dataStore
import com.example.tioro.data.local.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class TioroNotificationWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefsRepo = UserPreferencesRepository(appContext.dataStore)
        val notificationsEnabled = prefsRepo.notificationsEnabledFlow.first()
        
        if (!notificationsEnabled) {
            return Result.success()
        }

        val mode = inputData.getString("MODE") ?: return Result.failure()
        val hour = inputData.getInt("HOUR", -1)
        val today = LocalDate.now().toString()
        val dao = AppDatabase.getDatabase(appContext).dailyRecordDao()

        when (mode) {
            "BLANCO" -> {
                val logs = dao.getLogsByDateSnapshot(today)
                if (logs.isEmpty()) {
                    val (title, content) = when (hour) {
                        9 -> appContext.getString(R.string.notif_blanco_9_title) to appContext.getString(R.string.notif_blanco_9_sub)
                        12 -> appContext.getString(R.string.notif_blanco_12_title) to appContext.getString(R.string.notif_blanco_12_sub)
                        15 -> appContext.getString(R.string.notif_blanco_15_title) to appContext.getString(R.string.notif_blanco_15_sub)
                        18 -> appContext.getString(R.string.notif_blanco_18_title) to appContext.getString(R.string.notif_blanco_18_sub)
                        else -> appContext.getString(R.string.notif_blanco_default_title) to appContext.getString(R.string.notif_blanco_default_sub)
                    }
                    TioroNotificationManager.showNotification(appContext, title, content)
                }
            }
            
            "CIERRE" -> {
                val logs = dao.getLogsByDateSnapshot(today)
                val allCompleted = logs.isNotEmpty() && logs.all { it.isCompleted }
                if (!allCompleted) {
                    val (title, content) = when (hour) {
                        23 -> appContext.getString(R.string.notif_cierre_23_title) to appContext.getString(R.string.notif_cierre_23_sub)
                        else -> appContext.getString(R.string.notif_cierre_2345_title) to appContext.getString(R.string.notif_cierre_2345_sub) // 23:45
                    }
                    TioroNotificationManager.showNotification(appContext, title, content)
                }
            }
            
            "DOMINGO" -> {
                if (LocalDate.now().dayOfWeek == DayOfWeek.SUNDAY) {
                    val currentMonday = LocalDate.now().with(DayOfWeek.MONDAY).toString()
                    val objectives = dao.getObjectivesForWeekSnapshot(currentMonday)
                    val allCompleted = objectives.isNotEmpty() && objectives.all { it.isCompleted }
                    if (!allCompleted) {
                        TioroNotificationManager.showNotification(
                            appContext,
                            appContext.getString(R.string.notif_domingo_title),
                            appContext.getString(R.string.notif_domingo_sub)
                        )
                    }
                }
            }
            
            "FOLLOW_UP" -> {
                val currentHour = LocalTime.now().hour
                if (currentHour !in 0..8) {
                    val logs = dao.getLogsByDateSnapshot(today)
                    if (logs.any { !it.isCompleted }) {
                        TioroNotificationManager.showNotification(
                            appContext,
                            appContext.getString(R.string.notif_followup_title),
                            appContext.getString(R.string.notif_followup_sub)
                        )
                        // Reprogramamos otras 5 horas
                        TioroNotificationManager.scheduleFollowUpReminder(appContext)
                    }
                }
            }

            "WIDGET_DISCOVERY" -> {
                val appWidgetManager = android.appwidget.AppWidgetManager.getInstance(appContext)
                val mainWidgetIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(appContext, TioroWidgetProvider::class.java))
                val streakWidgetIds = appWidgetManager.getAppWidgetIds(android.content.ComponentName(appContext, TioroStreakWidgetProvider::class.java))
                val hasAnyWidgetPlaced = mainWidgetIds.isNotEmpty() || streakWidgetIds.isNotEmpty()
                
                if (!hasAnyWidgetPlaced) {
                    TioroNotificationManager.showNotification(
                        appContext,
                        appContext.getString(R.string.notif_widget_discovery_title),
                        appContext.getString(R.string.notif_widget_discovery_sub)
                    )
                }
            }
        }

        // Si fue una franja fija (todo menos FOLLOW_UP), reprogramarla para su siguiente ciclo (al día o semana siguiente)
        if (mode != "FOLLOW_UP") {
            TioroNotificationManager.scheduleAllDailyReminders(appContext)
        }

        return Result.success()
    }
}
