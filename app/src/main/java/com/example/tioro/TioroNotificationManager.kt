package com.example.tioro

import io.github.mortadelaco.tioro.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.Calendar
import java.util.concurrent.TimeUnit

object TioroNotificationManager {
    const val CHANNEL_ID = "tioro_reminders"
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notif_channel_name)
            val descriptionText = context.getString(R.string.notif_channel_desc)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showNotification(context: Context, title: String, content: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: Cambiar por icono de la app si tienes uno
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    
    // Calcula retraso hasta próxima hora fijada. Si ya pasó hoy, calcula para mañana.
    private fun getDelayToNextFranja(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
    
    private fun getDelayToNextSunday18h(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        if (target.before(now)) {
            target.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun getDelayToNextTuesday16h(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        if (target.before(now)) {
            target.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
    
    fun scheduleAllDailyReminders(context: Context) {
        val workManager = WorkManager.getInstance(context)
        
        fun scheduleFixedFranja(tag: String, hour: Int, minute: Int, mode: String) {
            val delay = getDelayToNextFranja(hour, minute)
            val inputData = workDataOf("MODE" to mode, "HOUR" to hour)
            val request = OneTimeWorkRequestBuilder<com.example.tioro.TioroNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(tag)
                .build()
                
            workManager.enqueueUniqueWork(tag, ExistingWorkPolicy.KEEP, request)
        }
        
        // Modo Día en Blanco
        scheduleFixedFranja("BLANCO_0900", 9, 0, "BLANCO")
        scheduleFixedFranja("BLANCO_1230", 12, 30, "BLANCO")
        scheduleFixedFranja("BLANCO_1530", 15, 30, "BLANCO")
        scheduleFixedFranja("BLANCO_1830", 18, 30, "BLANCO")
        
        // Modo Cierre Nocturno
        scheduleFixedFranja("CIERRE_2300", 23, 0, "CIERRE")
        scheduleFixedFranja("CIERRE_2345", 23, 45, "CIERRE")
        
        // Modo Domingo de Bloque
        val delaySunday = getDelayToNextSunday18h()
        val sundayData = workDataOf("MODE" to "DOMINGO")
        val sundayRequest = OneTimeWorkRequestBuilder<com.example.tioro.TioroNotificationWorker>()
            .setInitialDelay(delaySunday, TimeUnit.MILLISECONDS)
            .setInputData(sundayData)
            .addTag("DOMINGO_1800")
            .build()
        workManager.enqueueUniqueWork("DOMINGO_1800", ExistingWorkPolicy.KEEP, sundayRequest)

        // Modo Descubrimiento de Widgets (Martes 16:00)
        val delayTuesday = getDelayToNextTuesday16h()
        val tuesdayData = workDataOf("MODE" to "WIDGET_DISCOVERY")
        val tuesdayRequest = OneTimeWorkRequestBuilder<com.example.tioro.TioroNotificationWorker>()
            .setInitialDelay(delayTuesday, TimeUnit.MILLISECONDS)
            .setInputData(tuesdayData)
            .addTag("WIDGET_DISCOVERY_1600")
            .build()
        workManager.enqueueUniqueWork("WIDGET_DISCOVERY_1600", ExistingWorkPolicy.KEEP, tuesdayRequest)
    }
    
    fun scheduleFollowUpReminder(context: Context) {
        val inputData = workDataOf("MODE" to "FOLLOW_UP")
        val request = OneTimeWorkRequestBuilder<com.example.tioro.TioroNotificationWorker>()
            .setInitialDelay(5, TimeUnit.HOURS)
            .setInputData(inputData)
            .addTag("FOLLOW_UP")
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork("FOLLOW_UP", ExistingWorkPolicy.REPLACE, request)
    }
}
