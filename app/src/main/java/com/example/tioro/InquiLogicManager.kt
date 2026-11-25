package com.example.tioro

import kotlin.math.ceil
import kotlin.math.min

/**
 * Mánager principal para la lógica core del "Proyecto Inqui".
 * Esta clase contiene pura lógica de negocio y no tiene dependencias de Android,
 * facilitando así su testeo y mantenimiento.
 */
class InquiLogicManager {

    /**
     * Calcula la puntuación diaria basándose en las actividades de ocio, actividades de esfuerzo y proyectos.
     *
     * @param leisureCount Cantidad de actividades de ocio normales realizadas.
     * @param effortCount Cantidad de actividades de esfuerzo (Actividades+) realizadas.
     * @param projectPoints Puntos obtenidos mediante proyectos (rango esperado de 0.0 a 20.0).
     * @param leisureWeight Multiplicador de puntos por cada actividad de ocio (por defecto 15.0f).
     * @return Float con la nota final calculada para el día.
     */
    fun calculateDailyScore(
        leisureCount: Int,
        effortCount: Int,
        projectPoints: Float,
        leisureWeight: Float = 15.0f
    ): Float {
        // Requisito de esfuerzo (R): ceil(leisureCount / 3.0)
        // Se divide entre 3.0 (Double) para no perder decimales antes de aplicar la función techo.
        val effortRequirement = ceil(leisureCount / 3.0).toInt()

        // Excedente (E): effortCount - R
        val surplus = effortCount - effortRequirement

        // Puntuación Ocio (S_ocio): min(leisureCount * leisureWeight, 49.0f)
        val leisureScore = min(leisureCount * leisureWeight, 49.0f)

        // Puntuación Obligatoria (S_obliga) basada en el excedente (E)
        val obligatoryScore = when {
            surplus < 0 -> 0.0f
            surplus == 0 -> 51.0f
            surplus == 1 -> 56.1f
            else -> 61.2f // Si E >= 2
        }

        // Total del día: se evalúa si hay puntos de proyecto para aplicar la fórmula correspondiente
        return if (projectPoints > 0.0f) {
            ((leisureScore + obligatoryScore) * 0.8f) + projectPoints
        } else {
            leisureScore + obligatoryScore
        }
    }
}
