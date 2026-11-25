package com.example.tioro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_block_objectives")
data class WorkBlockObjective(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weekStartDate: String, // El lunes de la semana a la que pertenece el bloque en formato "YYYY-MM-DD"
    val blockTitle: String,    // Nombre o título del bloque de trabajo
    val objectiveName: String, // Texto del objetivo mayoritario
    val isCompleted: Boolean = false,
    val completedDate: String? = null // "YYYY-MM-DD" de cuando se completó
)
