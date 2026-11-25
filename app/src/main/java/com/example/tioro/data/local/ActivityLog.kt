package com.example.tioro.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    foreignKeys = [
        ForeignKey(
            entity = DailyRecord::class,
            parentColumns = ["id"],
            childColumns = ["dailyRecordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dailyRecordId")]
)
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dailyRecordId: Int,
    val templateId: Int? = null,
    val name: String,
    val category: String, // "Actividad", "Actividad+"
    val date: String, // "YYYY-MM-DD"
    val isCompleted: Boolean = false
)
