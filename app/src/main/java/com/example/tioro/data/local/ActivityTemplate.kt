package com.example.tioro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_templates")
data class ActivityTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String, // "Actividad", "Actividad+"
    val recurrenceDays: String // ej. "1,2,3,4,5,6,7"
)
