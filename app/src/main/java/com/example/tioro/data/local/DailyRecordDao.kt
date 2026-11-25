package com.example.tioro.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class StatsResult(
    val totalCount: Int?,
    val totalCompleted: Int?,
    val leisureCount: Int?,
    val leisureCompleted: Int?,
    val effortCount: Int?,
    val effortCompleted: Int?
) {
    val safeTotalCount get() = totalCount ?: 0
    val safeTotalCompleted get() = totalCompleted ?: 0
    val safeLeisureCount get() = leisureCount ?: 0
    val safeLeisureCompleted get() = leisureCompleted ?: 0
    val safeEffortCount get() = effortCount ?: 0
    val safeEffortCompleted get() = effortCompleted ?: 0
}

@Dao
interface DailyRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("SELECT * FROM activity_logs WHERE dailyRecordId = :recordId ORDER BY id DESC")
    fun getLogsForRecord(recordId: Int): Flow<List<ActivityLog>>

    @Query("SELECT COUNT(*) FROM activity_logs WHERE date >= :start AND date <= :end")
    suspend fun getCountByDateRange(start: String, end: String): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE date >= :start AND date <= :end AND isCompleted = 1")
    suspend fun getCompletedCountByDateRange(start: String, end: String): Int

    @Query("DELETE FROM activity_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: Int)

    @Query("UPDATE activity_logs SET name = :newName WHERE id = :logId")
    suspend fun updateLogName(logId: Int, newName: String)

    @Query("UPDATE activity_logs SET isCompleted = :isCompleted WHERE id = :logId")
    suspend fun updateLogCompletion(logId: Int, isCompleted: Boolean)

    // --- Activity Templates ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: ActivityTemplate)

    @Query("SELECT * FROM activity_templates")
    fun getAllTemplates(): Flow<List<ActivityTemplate>>

    @Query("SELECT * FROM activity_templates WHERE category = 'Proyecto'")
    fun getAllProjectTemplates(): Flow<List<ActivityTemplate>>

    @Query("DELETE FROM activity_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int)

    @Query("UPDATE activity_templates SET name = :newName WHERE id = :templateId")
    suspend fun updateTemplateDetails(templateId: Int, newName: String)

    @Query("DELETE FROM activity_logs WHERE templateId = :templateId AND date >= :start AND date <= :end")
    suspend fun deleteLogsByTemplateIdToday(templateId: Int, start: String, end: String)

    // Inserta un nuevo registro. Si ya existe uno con el mismo ID, lo reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DailyRecord)

    // Actualiza un registro existente
    @Update
    suspend fun update(record: DailyRecord)

    // Obtener los últimos 7 días.
    // Ordenamos por fecha descendente y limitamos a 7 resultados.
    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT 1")
    suspend fun getLastRecord(): DailyRecord?

    @Query("SELECT AVG(dailyScore) FROM daily_records WHERE date >= :start AND date <= :end")
    fun getAnnualAverage(start: String, end: String): Flow<Float?>

    @Query("SELECT * FROM daily_records WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun getAnnualHistory(start: String, end: String): Flow<List<DailyRecord>>

    @Query("""
        SELECT 
            COUNT(id) as totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as totalCompleted,
            SUM(CASE WHEN category = 'Actividad' THEN 1 ELSE 0 END) as leisureCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad' THEN 1 ELSE 0 END) as leisureCompleted,
            SUM(CASE WHEN category = 'Actividad+' THEN 1 ELSE 0 END) as effortCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad+' THEN 1 ELSE 0 END) as effortCompleted
        FROM activity_logs
        WHERE date >= :start AND date <= :end
    """)
    fun getAnnualCategoryStats(start: String, end: String): Flow<StatsResult?>
    
    @Query("SELECT AVG(dailyScore) FROM daily_records WHERE date >= :start AND date <= :end")
    fun getSevenDayAverage(start: String, end: String): Flow<Float?>

    @Query("SELECT * FROM daily_records WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun getSevenDayHistory(start: String, end: String): Flow<List<DailyRecord>>

    @Query("SELECT AVG(dailyScore) FROM daily_records WHERE date >= :start AND date <= :end")
    fun getMonthlyAverage(start: String, end: String): Flow<Float?>

    @Query("SELECT * FROM daily_records WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun getMonthlyHistory(start: String, end: String): Flow<List<DailyRecord>>

    @Query("""
        SELECT 
            COUNT(id) as totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as totalCompleted,
            SUM(CASE WHEN category = 'Actividad' THEN 1 ELSE 0 END) as leisureCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad' THEN 1 ELSE 0 END) as leisureCompleted,
            SUM(CASE WHEN category = 'Actividad+' THEN 1 ELSE 0 END) as effortCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad+' THEN 1 ELSE 0 END) as effortCompleted
        FROM activity_logs
        WHERE date >= :start AND date <= :end
    """)
    fun getMonthlyCategoryStats(start: String, end: String): Flow<StatsResult?>

    @Query("""
        SELECT 
            COUNT(id) as totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as totalCompleted,
            SUM(CASE WHEN category = 'Actividad' THEN 1 ELSE 0 END) as leisureCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad' THEN 1 ELSE 0 END) as leisureCompleted,
            SUM(CASE WHEN category = 'Actividad+' THEN 1 ELSE 0 END) as effortCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad+' THEN 1 ELSE 0 END) as effortCompleted
        FROM activity_logs
        WHERE date >= :start AND date <= :end
    """)
    fun getWeeklyCategoryStats(start: String, end: String): Flow<StatsResult?>

    @Query("""
        SELECT 
            COUNT(id) as totalCount,
            SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as totalCompleted,
            SUM(CASE WHEN category = 'Actividad' THEN 1 ELSE 0 END) as leisureCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad' THEN 1 ELSE 0 END) as leisureCompleted,
            SUM(CASE WHEN category = 'Actividad+' THEN 1 ELSE 0 END) as effortCount,
            SUM(CASE WHEN isCompleted = 1 AND category = 'Actividad+' THEN 1 ELSE 0 END) as effortCompleted
        FROM activity_logs
        WHERE date >= :start AND date <= :end
    """)
    suspend fun getLogStatsSnapshot(start: String, end: String): StatsResult?

    @Query("SELECT * FROM daily_records ORDER BY date DESC")
    fun getAllRecordsDesc(): Flow<List<DailyRecord>>

    @Query("SELECT * FROM daily_records ORDER BY date DESC LIMIT 7")
    fun getLast7DaysRecords(): Flow<List<DailyRecord>>
    
    @Query("SELECT * FROM daily_records WHERE date = :dateString LIMIT 1")
    suspend fun getRecordByDate(dateString: String): DailyRecord?

    @Query("SELECT * FROM activity_logs WHERE date = :date ORDER BY id DESC")
    fun getLogsByDate(date: String): Flow<List<ActivityLog>>

    @Query("SELECT * FROM activity_logs WHERE date = :date ORDER BY id DESC")
    suspend fun getLogsByDateSnapshot(date: String): List<ActivityLog>

    // --- WorkBlockObjective Queries ---
    @Query("SELECT * FROM work_block_objectives WHERE weekStartDate = :weekStart ORDER BY id ASC")
    fun getObjectivesForWeek(weekStart: String): Flow<List<WorkBlockObjective>>

    @Query("SELECT * FROM work_block_objectives WHERE weekStartDate = :weekStart ORDER BY id ASC")
    suspend fun getObjectivesForWeekSnapshot(weekStart: String): List<WorkBlockObjective>

    @Query("SELECT * FROM work_block_objectives WHERE date(weekStartDate) >= date(:start) AND date(weekStartDate) <= date(:end)")
    suspend fun getObjectivesInRangeSnapshot(start: String, end: String): List<WorkBlockObjective>

    @Query("SELECT * FROM work_block_objectives ORDER BY weekStartDate DESC, id ASC")
    suspend fun getAllObjectivesSnapshot(): List<WorkBlockObjective>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjective(obj: WorkBlockObjective)

    @Query("UPDATE work_block_objectives SET isCompleted = :completed, completedDate = :completedDate WHERE id = :id")
    suspend fun updateObjectiveCompletion(id: Int, completed: Boolean, completedDate: String?)

    @Query("DELETE FROM work_block_objectives WHERE id = :id")
    suspend fun deleteObjectiveById(id: Int)

    @Query("SELECT * FROM daily_records WHERE date >= :start AND date <= :end ORDER BY date ASC")
    suspend fun getRecordsInRangeSnapshot(start: String, end: String): List<DailyRecord>

    // --- Backup / Export / Import Queries ---
    @Query("SELECT * FROM daily_records ORDER BY date ASC")
    suspend fun getAllDailyRecordsSnapshot(): List<DailyRecord>

    @Query("SELECT * FROM activity_logs ORDER BY id ASC")
    suspend fun getAllActivityLogsSnapshot(): List<ActivityLog>

    @Query("SELECT * FROM activity_templates ORDER BY id ASC")
    suspend fun getAllTemplatesSnapshot(): List<ActivityTemplate>

    @Query("DELETE FROM daily_records")
    suspend fun clearAllDailyRecords()

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllActivityLogs()

    @Query("DELETE FROM activity_templates")
    suspend fun clearAllTemplates()

    @Query("DELETE FROM work_block_objectives")
    suspend fun clearAllObjectives()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDailyRecords(records: List<DailyRecord>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllActivityLogs(logs: List<ActivityLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTemplates(templates: List<ActivityTemplate>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllObjectives(objectives: List<WorkBlockObjective>)
}
