# Changelog

Todos los cambios notables de **Tioro** se documentarán en este archivo.

## [v1.0-beta] - 2026-11-25

### Añadido
- **Motor Matemático Diario y Semanal:** Reparto diario entre Actividades (36.25%) y Actividades+ (63.75%), y media semanal ponderada (70% días transcurridos + 30% Bloque de Trabajo).
- **Bloques de Trabajo Semanales:** Sustitución de proyectos por bloques semanales directos con arrastre automático de objetivos pendientes y bloqueo al alcanzar el 100%.
- **Estadísticas Reales (Semanal, Mensual y Anual):** Gráficas en `Canvas` sincronizadas con `java.time.LocalDate`, penalización estricta por días vacíos y sistema de medallas.
- **Calendario de Rachas:** Modal interactivo con navegación mensual, racha aprobada a partir del `50%` y barra de metas.
- **Backup ("Máquina del Tiempo"):** Edición retroactiva de días pasados con `DatePicker`, recálculo en vivo de métricas y restauración automática de rachas.
- **Notificaciones Inteligentes (WorkManager):** Avisos dependientes del estado real en Room (modo día en blanco, seguimiento cada 5h con silencio nocturno, cierre a las 23:00/23:45 y recordatorio dominical).
- **Portabilidad y Escritorio:** Exportación/importación completa de base de datos en `.json`, resumen semanal los lunes y widget nativo de escritorio.
- **UX/UI Dark & Gold:** Navegación sin parpadeos, entrada en cascada, gráficas animadas y feedback háptico.
