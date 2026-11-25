package com.example.tioro.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyRecord::class, ActivityLog::class, ActivityTemplate::class, WorkBlockObjective::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dailyRecordDao(): DailyRecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia no es nula la devolvemos, de lo contrario la creamos de forma segura
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tioro_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = false)
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}
