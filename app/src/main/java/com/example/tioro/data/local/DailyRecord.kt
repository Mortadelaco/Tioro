package com.example.tioro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_records")
data class DailyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String, // String formateado como "YYYY-MM-DD"
    val leisureCount: Int,
    val effortCount: Int,
    val projectPoints: Float,
    val baseScore: Float = 0f,
    val bonusScore: Float = 0f,
    val dailyScore: Float
)
