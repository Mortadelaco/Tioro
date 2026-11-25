package com.example.tioro

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
                        9 -> "¿Otro día improvisando? 👀" to "Abre Tioro y registra qué vas a hacer hoy antes de que se te escape la mañana."
                        12 -> "Mediodía y tu lista sigue en blanco... 🤨" to "No me hagas empezar a contar horas muertas. Registra tus actividades."
                        15 -> "¿Post-comida sin plan? 🛋️" to "Acuérdate de registrar tus actividades de hoy. Tu media semanal está mirando."
                        18 -> "Última llamada de la tarde ⏳" to "Todavía puedes salvar el día, pero si no registras nada, el cero no perdona."
                        else -> "Recuerda registrar tus tareas" to "Abre Inqui y ponte al día."
                    }
                    TioroNotificationManager.showNotification(appContext, title, content)
                }
            }
            
            "CIERRE" -> {
                val logs = dao.getLogsByDateSnapshot(today)
                val allCompleted = logs.isNotEmpty() && logs.all { it.isCompleted }
                if (!allCompleted) {
                    val (title, content) = when (hour) {
                        23 -> "El día agoniza... 🔥" to "Queda una hora para medianoche. Termina de actualizar tus actividades o despídete de la nota."
                        else -> "15 minutos para medianoche 🚨" to "Último aviso antes de dormir: cierra tus actividades de hoy o mañana tocará dar explicaciones." // 23:45
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
                            "Es domingo y tu Bloque de Trabajo te mira mal 👀",
                            "Tienes hasta medianoche para actualizar tus objetivos semanales (recuerda: vale el 30% de tu semana)."
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
                            "¿Cómo va eso o solo las pusiste de adorno? 👀",
                            "Llevas 5 horas desde tu último registro. Entra y tacha lo que ya hayas hecho."
                        )
                        // Reprogramamos otras 5 horas
                        TioroNotificationManager.scheduleFollowUpReminder(appContext)
                    }
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
