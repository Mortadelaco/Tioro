package com.example.tioro

import androidx.lifecycle.ViewModel
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.tioro.data.local.ActivityLog
import com.example.tioro.data.local.ActivityTemplate
import com.example.tioro.data.local.DailyRecord
import com.example.tioro.data.local.DailyRecordDao
import com.example.tioro.data.local.StatsResult
import com.example.tioro.data.local.UserPreferencesRepository
import com.example.tioro.data.local.WorkBlockObjective
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.ChronoUnit

data class ActivityLogItemData(
    val id: Int,
    val name: String,
    val date: String,
    val category: String,
    val isCompleted: Boolean
)

data class MondayRecapData(
    val lastWeekFinalScore: Float,
    val lastWeekEarnedMedal: Boolean,
    val carriedOverObjectivesCount: Int
)

data class InquiUiState(
    val isAppLoading: Boolean = true,
    val userName: String? = null,
    val leisureCount: Int = 0,
    val effortCount: Int = 0,
    val baseScore: Float = 0f,
    val bonusScore: Float = 0f,
    val dailyScore: Float = 0f,
    val currentStreak: Int = 0,
    val isStreakSavedToday: Boolean = false,
    val streakHistory: List<Boolean> = List(7) { false },
    val logs: List<ActivityLogItemData> = emptyList(),
    
    // Novedad: Bloques de Trabajo Semanales
    val currentWeekObjectives: List<WorkBlockObjective> = emptyList(),
    val isBlockLockedForWeek: Boolean = false,

    val backupSelectedDate: String = LocalDate.now().minusDays(1).toString(),
    val backupLogs: List<ActivityLogItemData> = emptyList(),
    val backupDayScore: Float = 0f,
    
    val notificationsEnabled: Boolean = true,
    val mondayRecapData: MondayRecapData? = null,
    
    val weeklyAverage: Float = 0f,
    val weeklyLeisurePct: Float = 0f,
    val weeklyEffortPct: Float = 0f,
    val weeklyProjectsPct: Float = 0f, // Reutilizado para Bloque de Trabajo
    val weeklyActivityChartData: List<Float> = List(7) { 0f },
    
    val monthlyAverage: Float = 0f,
    val monthlyLeisurePct: Float = 0f,
    val monthlyEffortPct: Float = 0f,
    val monthlyProjectsPct: Float = 0f, // Reutilizado para Bloque de Trabajo
    val monthlyActivityChartData: List<Float> = List(4) { 0f },
    val monthlyMedals: List<Boolean> = List(5) { false },
    
    val annualAverage: Float = 0f,
    val annualLeisurePct: Float = 0f,
    val annualEffortPct: Float = 0f,
    val annualProjectsPct: Float = 0f, // Reutilizado para Bloque de Trabajo
    val annualActivityChartData: List<Float> = List(12) { 0f },
    val annualMedals: List<Boolean> = List(12) { false }
)

class InquiViewModel(
    private val dao: DailyRecordDao,
    private val userPrefs: UserPreferencesRepository,
    private val appContext: android.content.Context? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(InquiUiState())
    val uiState: StateFlow<InquiUiState> = _uiState.asStateFlow()

    private var currentRecord: DailyRecord? = null
    private var logsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            userPrefs.userNameFlow.collect { name ->
                _uiState.value = _uiState.value.copy(userName = name, isAppLoading = false)
            }
        }
        viewModelScope.launch {
            userPrefs.notificationsEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
            }
        }
        syncAndLoadTodayRecord()
    }

    fun saveUserName(name: String) {
        viewModelScope.launch {
            userPrefs.saveUserName(name)
        }
    }
    
    suspend fun getRecordsInRange(start: String, end: String): List<DailyRecord> {
        return dao.getRecordsInRangeSnapshot(start, end)
    }
    
    fun saveNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPrefs.saveNotificationsEnabled(enabled)
        }
    }

    fun reloadAllDataAfterImport() {
        syncAndLoadTodayRecord()
    }

    fun dismissMondayRecap() {
        val currentMondayStr = LocalDate.now().with(DayOfWeek.MONDAY).toString()
        viewModelScope.launch {
            userPrefs.saveLastRecapWeek(currentMondayStr)
            _uiState.value = _uiState.value.copy(mondayRecapData = null)
        }
    }

    suspend fun exportBackupJson(context: android.content.Context): String {
        return TioroBackupExporter.exportToJson(context, dao, userPrefs)
    }

    suspend fun importBackupJson(context: android.content.Context, jsonString: String): Boolean {
        val success = TioroBackupExporter.importFromJson(context, dao, userPrefs, jsonString)
        if (success) {
            reloadAllDataAfterImport()
        }
        return success
    }

    private fun getTodayDateString(): String {
        return LocalDate.now().toString()
    }

    private fun syncAndLoadTodayRecord() {
        viewModelScope.launch {
            val todayStr = getTodayDateString()
            val today = LocalDate.now()
            val currentMonday = today.with(DayOfWeek.MONDAY).toString()

            // 1. Arrastre de objetivos no completados
            val allObjectives = dao.getAllObjectivesSnapshot()
            val currentWeekObjs = allObjectives.filter { it.weekStartDate == currentMonday }
            if (currentWeekObjs.isEmpty()) {
                val pastObjectives = allObjectives.filter { LocalDate.parse(it.weekStartDate).isBefore(today.with(DayOfWeek.MONDAY)) }
                val lastWeekDate = pastObjectives.maxByOrNull { it.weekStartDate }?.weekStartDate
                if (lastWeekDate != null) {
                    val pendingLastWeek = pastObjectives.filter { it.weekStartDate == lastWeekDate && !it.isCompleted }
                    if (pendingLastWeek.isNotEmpty()) {
                        val blockTitleToUse = pendingLastWeek.first().blockTitle
                        pendingLastWeek.forEach { oldObj ->
                            dao.insertObjective(
                                WorkBlockObjective(
                                    weekStartDate = currentMonday,
                                    blockTitle = blockTitleToUse,
                                    objectiveName = oldObj.objectiveName,
                                    isCompleted = false,
                                    completedDate = null
                                )
                            )
                        }
                    }
                }
            }

            // 2. Resumen de Cierre de Semana los Lunes
            if (today.dayOfWeek == DayOfWeek.MONDAY) {
                val lastRecapWeek = userPrefs.lastRecapWeekFlow.first()
                if (lastRecapWeek != currentMonday) {
                    val lastMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY)
                    val lastSunday = lastMonday.plusDays(6)
                    val lastWeekRecords = dao.getRecordsInRangeSnapshot(lastMonday.toString(), lastSunday.toString())
                    val lastWeekObjs = dao.getObjectivesForWeekSnapshot(lastMonday.toString())
                    
                    val dailyAvg = if (lastWeekRecords.isNotEmpty()) {
                        lastWeekRecords.sumOf { it.dailyScore.toDouble() }.toFloat() / 7f
                    } else 0f
                    
                    val blockPct = if (lastWeekObjs.isNotEmpty()) {
                        lastWeekObjs.count { it.isCompleted }.toFloat() / lastWeekObjs.size.toFloat()
                    } else 0f
                    
                    val lastWeekFinalScore = (dailyAvg * 0.70f) + (blockPct * 100f * 0.30f)
                    val lastWeekEarnedMedal = lastWeekFinalScore >= 60f
                    val carriedOverCount = lastWeekObjs.count { !it.isCompleted }

                    if (lastWeekRecords.isNotEmpty() || lastWeekObjs.isNotEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            mondayRecapData = MondayRecapData(
                                lastWeekFinalScore = lastWeekFinalScore,
                                lastWeekEarnedMedal = lastWeekEarnedMedal,
                                carriedOverObjectivesCount = carriedOverCount
                            )
                        )
                    }
                }
            }

            var record = dao.getRecordByDate(todayStr)
            if (record == null) {
                record = DailyRecord(
                    date = todayStr,
                    leisureCount = 0,
                    effortCount = 0,
                    projectPoints = 0f,
                    baseScore = 0f,
                    bonusScore = 0f,
                    dailyScore = 0f
                )
                dao.insert(record)
                record = dao.getRecordByDate(todayStr)
            }
            
            currentRecord = record
            updateUiState()
            
            generateTasksForToday(record!!)

            recalculateStreak()
            recalculateAllStats()

            logsJob?.cancel()
            record?.let { rec ->
                logsJob = launch {
                    dao.getLogsForRecord(rec.id).collect { logs ->
                        val formattedLogs = logs.map { log ->
                            ActivityLogItemData(log.id, log.name, log.date, log.category, log.isCompleted)
                        }
                        
                        _uiState.value = _uiState.value.copy(
                            logs = formattedLogs
                        )
                    }
                }
            }
        }
    }

    private suspend fun recalculateStreak() {
        var streak = 0
        var savedToday = false
        var offset = 0
        
        while (offset < 3650) {
            val dateToCheck = LocalDate.now().minusDays(offset.toLong()).toString()
            val r = dao.getRecordByDate(dateToCheck)
            
            if (r != null && r.dailyScore >= 50.0f) {
                streak++
                if (offset == 0) savedToday = true
            } else {
                if (offset != 0) break
            }
            offset++
        }

        val sevenDaysHistory = BooleanArray(7) { false }
        for (i in 0..6) {
            val dateToCheck = LocalDate.now().minusDays(i.toLong()).toString()
            val r = dao.getRecordByDate(dateToCheck)
            sevenDaysHistory[6 - i] = (r != null && r.dailyScore >= 50.0f)
        }
        
        _uiState.value = _uiState.value.copy(
            currentStreak = streak,
            isStreakSavedToday = savedToday,
            streakHistory = sevenDaysHistory.toList()
        )
    }

    private suspend fun generateTasksForToday(record: DailyRecord) {
        val todayLogs = dao.getLogsForRecord(record.id).first()
        if (todayLogs.isNotEmpty()) {
            return
        }
        
        val dayOfWeek = LocalDate.now().dayOfWeek.value.toString()
        val allTemplates = dao.getAllTemplates().first()
        
        val templatesForToday = allTemplates.filter { it.recurrenceDays.contains(dayOfWeek) }
        
        for (template in templatesForToday) {
            dao.insertLog(
                ActivityLog(
                    dailyRecordId = record.id,
                    templateId = template.id,
                    name = template.name,
                    category = template.category,
                    date = getTodayDateString(),
                    isCompleted = false
                )
            )
        }
        updateTodayScore()
    }

    private fun updateTodayScore() {
        val record = currentRecord ?: return
        viewModelScope.launch {
            val start = getTodayDateString()
            val end = getTodayDateString()
            
            val stats = dao.getLogStatsSnapshot(start, end) ?: StatsResult(0,0,0,0,0,0)
            
            val safeLeisureCount = stats.safeLeisureCount
            val safeLeisureCompleted = stats.safeLeisureCompleted
            val safeEffortCount = stats.safeEffortCount
            val safeEffortCompleted = stats.safeEffortCompleted

            val leisureScore = if (safeLeisureCount > 0) {
                (safeLeisureCompleted.toFloat() / safeLeisureCount) * 36.25f
            } else 0f
            
            val effortScore = if (safeEffortCount > 0) {
                (safeEffortCompleted.toFloat() / safeEffortCount) * 63.75f
            } else 0f
            
            val totalScore = Math.round(leisureScore + effortScore).toFloat().coerceIn(0f, 100f)
            
            val updated = record.copy(
                dailyScore = totalScore
            )
            
            currentRecord = updated
            _uiState.value = _uiState.value.copy(
                dailyScore = totalScore,
                leisureCount = safeLeisureCount,
                effortCount = safeEffortCount
            )
            dao.update(updated)
            
            recalculateStreak()
            recalculateAllStats()
        }
    }

    private suspend fun recalculateAllStats() {
        val today = LocalDate.now()
        val currentMonday = today.with(DayOfWeek.MONDAY)
        val currentMondayStr = currentMonday.toString()
        
        // Bloque de trabajo actual
        val currentObjs = dao.getObjectivesForWeekSnapshot(currentMondayStr)
        val isLocked = currentObjs.isNotEmpty() && currentObjs.all { it.isCompleted }
        
        _uiState.value = _uiState.value.copy(
            currentWeekObjectives = currentObjs,
            isBlockLockedForWeek = isLocked
        )

        // --- Weekly Stats ---
        val aPointsWeek = FloatArray(7) { 0f }
        var sumDailyWeek = 0f
        val daysElapsedThisWeek = ChronoUnit.DAYS.between(currentMonday, today).toInt() + 1
        
        val weeklyRecords = dao.getRecordsInRangeSnapshot(currentMondayStr, currentMonday.plusDays(6).toString())
        for (i in 0..6) {
            val d = currentMonday.plusDays(i.toLong())
            if (d.isAfter(today)) {
                aPointsWeek[i] = 0f
            } else {
                val r = weeklyRecords.find { it.date == d.toString() }
                if (r != null) {
                    aPointsWeek[i] = (r.dailyScore / 100f).coerceIn(0f, 1f)
                    sumDailyWeek += r.dailyScore
                } else {
                    aPointsWeek[i] = 0f
                }
            }
        }
        val dailyWeekAvg = sumDailyWeek / daysElapsedThisWeek.toFloat()
        
        val blockPctWeek = if (currentObjs.isNotEmpty()) {
            currentObjs.count { it.isCompleted }.toFloat() / currentObjs.size.toFloat()
        } else 0f
        
        val weeklyAverage = (dailyWeekAvg * 0.70f) + (blockPctWeek * 100f * 0.30f)

        val statsWeek = dao.getLogStatsSnapshot(currentMondayStr, today.toString()) ?: StatsResult(0,0,0,0,0,0)
        val weeklyEffortPct = if (statsWeek.safeEffortCount > 0) (statsWeek.safeEffortCompleted.toFloat() / statsWeek.safeEffortCount).coerceIn(0f, 1f) else 0f
        val weeklyLeisurePct = if (statsWeek.safeLeisureCount > 0) (statsWeek.safeLeisureCompleted.toFloat() / statsWeek.safeLeisureCount).coerceIn(0f, 1f) else 0f

        // --- Monthly Stats ---
        // Últimas 4 semanas (de lunes a domingo) incluyendo la actual
        val aPointsMonth = FloatArray(4) { 0f }
        var sumMonthAvgs = 0f
        var totalMonthObjs = 0
        var completedMonthObjs = 0
        
        for (w in 0..3) {
            val wMonday = currentMonday.minusWeeks((3 - w).toLong())
            val wSunday = wMonday.plusDays(6)
            
            val daysInThatWeek = if (wMonday == currentMonday) daysElapsedThisWeek else 7
            val wRecords = dao.getRecordsInRangeSnapshot(wMonday.toString(), wSunday.toString())
            var wSum = 0f
            for (i in 0 until daysInThatWeek) {
                val d = wMonday.plusDays(i.toLong())
                val r = wRecords.find { it.date == d.toString() }
                if (r != null) wSum += r.dailyScore
            }
            val wDailyAvg = wSum / daysInThatWeek.toFloat()
            
            val wObjs = dao.getObjectivesForWeekSnapshot(wMonday.toString())
            totalMonthObjs += wObjs.size
            completedMonthObjs += wObjs.count { it.isCompleted }
            
            val wBlockPct = if (wObjs.isNotEmpty()) wObjs.count { it.isCompleted }.toFloat() / wObjs.size.toFloat() else 0f
            val wAvgFinal = (wDailyAvg * 0.70f) + (wBlockPct * 100f * 0.30f)
            
            aPointsMonth[w] = (wAvgFinal / 100f).coerceIn(0f, 1f)
            sumMonthAvgs += wAvgFinal
        }
        
        val monthlyAverage = sumMonthAvgs / 4f
        val monthlyProjectsPct = if (totalMonthObjs > 0) completedMonthObjs.toFloat() / totalMonthObjs.toFloat() else 0f

        val monthStartStr = currentMonday.minusWeeks(3).toString()
        val statsMonth = dao.getLogStatsSnapshot(monthStartStr, today.toString()) ?: StatsResult(0,0,0,0,0,0)
        val monthlyEffortPct = if (statsMonth.safeEffortCount > 0) (statsMonth.safeEffortCompleted.toFloat() / statsMonth.safeEffortCount).coerceIn(0f, 1f) else 0f
        val monthlyLeisurePct = if (statsMonth.safeLeisureCount > 0) (statsMonth.safeLeisureCompleted.toFloat() / statsMonth.safeLeisureCount).coerceIn(0f, 1f) else 0f

        // --- Annual Stats ---
        val aPointsYear = FloatArray(12) { 0f }
        val medalsYear = MutableList(12) { false }
        var sumYearAvgs = 0f
        var monthsElapsed = today.monthValue
        var totalYearObjs = 0
        var completedYearObjs = 0
        
        for (m in 1..12) {
            if (m > today.monthValue) {
                aPointsYear[m - 1] = 0f
                medalsYear[m - 1] = false
            } else {
                val firstDayOfMonth = today.withMonth(m).withDayOfMonth(1)
                val daysInMonth = if (m == today.monthValue) today.dayOfMonth else firstDayOfMonth.lengthOfMonth()
                val lastDayToConsider = firstDayOfMonth.plusDays((daysInMonth - 1).toLong())
                
                val mRecords = dao.getRecordsInRangeSnapshot(firstDayOfMonth.toString(), lastDayToConsider.toString())
                var mSum = 0f
                mRecords.forEach { mSum += it.dailyScore }
                val mDailyAvg = mSum / daysInMonth.toFloat()
                
                val mObjs = dao.getObjectivesInRangeSnapshot(firstDayOfMonth.toString(), lastDayToConsider.toString())
                totalYearObjs += mObjs.size
                completedYearObjs += mObjs.count { it.isCompleted }
                val mBlockPct = if (mObjs.isNotEmpty()) mObjs.count { it.isCompleted }.toFloat() / mObjs.size.toFloat() else 0f
                
                val mAvgFinal = (mDailyAvg * 0.70f) + (mBlockPct * 100f * 0.30f)
                
                aPointsYear[m - 1] = (mAvgFinal / 100f).coerceIn(0f, 1f)
                medalsYear[m - 1] = mAvgFinal >= 60f
                sumYearAvgs += mAvgFinal
            }
        }
        
        val annualAverage = sumYearAvgs / monthsElapsed.toFloat()
        val annualProjectsPct = if (totalYearObjs > 0) completedYearObjs.toFloat() / totalYearObjs.toFloat() else 0f

        val yearStartStr = today.withDayOfYear(1).toString()
        val statsYear = dao.getLogStatsSnapshot(yearStartStr, today.toString()) ?: StatsResult(0,0,0,0,0,0)
        val annualEffortPct = if (statsYear.safeEffortCount > 0) (statsYear.safeEffortCompleted.toFloat() / statsYear.safeEffortCount).coerceIn(0f, 1f) else 0f
        val annualLeisurePct = if (statsYear.safeLeisureCount > 0) (statsYear.safeLeisureCompleted.toFloat() / statsYear.safeLeisureCount).coerceIn(0f, 1f) else 0f

        _uiState.value = _uiState.value.copy(
            weeklyAverage = weeklyAverage,
            weeklyEffortPct = weeklyEffortPct,
            weeklyLeisurePct = weeklyLeisurePct,
            weeklyProjectsPct = blockPctWeek,
            weeklyActivityChartData = aPointsWeek.toList(),
            
            monthlyAverage = monthlyAverage,
            monthlyEffortPct = monthlyEffortPct,
            monthlyLeisurePct = monthlyLeisurePct,
            monthlyProjectsPct = monthlyProjectsPct,
            monthlyActivityChartData = aPointsMonth.toList(),
            // Las medallas mensuales se calculan si la media combinada de la semana es >= 60f
            monthlyMedals = aPointsMonth.map { (it * 100f) >= 60f }.toMutableList().apply { add(false) }, // 5 booleanos pide el estado
            
            annualAverage = annualAverage,
            annualEffortPct = annualEffortPct,
            annualLeisurePct = annualLeisurePct,
            annualProjectsPct = annualProjectsPct,
            annualActivityChartData = aPointsYear.toList(),
            annualMedals = medalsYear
        )

        appContext?.let { TioroWidgetProvider.updateAllWidgets(it) }
    }

    fun addActivity(name: String, isEffort: Boolean, context: android.content.Context) {
        val record = currentRecord ?: return
        viewModelScope.launch {
            dao.insertLog(
                ActivityLog(
                    dailyRecordId = record.id,
                    name = name,
                    category = if (isEffort) "Actividad+" else "Actividad",
                    date = getTodayDateString(),
                    isCompleted = false
                )
            )
            updateTodayScore()
            TioroNotificationManager.scheduleFollowUpReminder(context)
        }
    }

    private fun updateUiState() {
        currentRecord?.let {
            _uiState.value = _uiState.value.copy(
                dailyScore = it.dailyScore
            )
        }
    }

    fun toggleActivityCompletion(log: ActivityLogItemData) {
        val newCompleted = !log.isCompleted
        viewModelScope.launch {
            dao.updateLogCompletion(log.id, newCompleted)
            updateTodayScore()
        }
    }

    fun deleteLog(log: ActivityLogItemData) {
        viewModelScope.launch {
            dao.deleteLogById(log.id)
            updateTodayScore()
        }
    }

    fun updateLogName(logId: Int, newName: String) {
        viewModelScope.launch {
            dao.updateLogName(logId, newName)
        }
    }
    
    // --- WORK BLOCK OBJECTIVES ---
    
    fun addWorkBlockObjective(title: String, objectiveName: String) {
        viewModelScope.launch {
            dao.insertObjective(
                WorkBlockObjective(
                    weekStartDate = LocalDate.now().with(DayOfWeek.MONDAY).toString(),
                    blockTitle = title,
                    objectiveName = objectiveName,
                    isCompleted = false,
                    completedDate = null
                )
            )
            recalculateAllStats()
        }
    }
    
    fun toggleWorkBlockObjectiveCompletion(obj: WorkBlockObjective) {
        if (_uiState.value.isBlockLockedForWeek && !obj.isCompleted) return // No un-complete if locked
        val newStatus = !obj.isCompleted
        val completedDate = if (newStatus) LocalDate.now().toString() else null
        viewModelScope.launch {
            dao.updateObjectiveCompletion(obj.id, newStatus, completedDate)
            recalculateAllStats()
        }
    }
    
    fun deleteWorkBlockObjective(obj: WorkBlockObjective) {
        if (_uiState.value.isBlockLockedForWeek) return
        viewModelScope.launch {
            dao.deleteObjectiveById(obj.id)
            recalculateAllStats()
        }
    }

    // --- BACKUP (MÁQUINA DEL TIEMPO) ---

    fun selectBackupDate(dateString: String) {
        viewModelScope.launch {
            val record = dao.getRecordByDate(dateString)
            val logs = dao.getLogsByDateSnapshot(dateString).map { log ->
                ActivityLogItemData(log.id, log.name, log.date, log.category, log.isCompleted)
            }
            _uiState.value = _uiState.value.copy(
                backupSelectedDate = dateString,
                backupLogs = logs,
                backupDayScore = record?.dailyScore ?: 0f
            )
        }
    }

    fun addBackupActivity(dateString: String, name: String, isEffort: Boolean) {
        viewModelScope.launch {
            var record = dao.getRecordByDate(dateString)
            if (record == null) {
                record = DailyRecord(
                    date = dateString,
                    leisureCount = 0,
                    effortCount = 0,
                    projectPoints = 0f,
                    baseScore = 0f,
                    bonusScore = 0f,
                    dailyScore = 0f
                )
                dao.insert(record)
                record = dao.getRecordByDate(dateString)
            }
            if (record != null) {
                dao.insertLog(
                    ActivityLog(
                        dailyRecordId = record.id,
                        name = name,
                        category = if (isEffort) "Actividad+" else "Actividad",
                        date = dateString,
                        isCompleted = false
                    )
                )
                recalculateScoreForDate(dateString)
            }
        }
    }

    fun toggleBackupActivityCompletion(log: ActivityLogItemData) {
        viewModelScope.launch {
            dao.updateLogCompletion(log.id, !log.isCompleted)
            recalculateScoreForDate(log.date)
        }
    }

    fun deleteBackupActivity(log: ActivityLogItemData) {
        viewModelScope.launch {
            dao.deleteLogById(log.id)
            recalculateScoreForDate(log.date)
        }
    }

    private suspend fun recalculateScoreForDate(dateString: String) {
        val stats = dao.getLogStatsSnapshot(dateString, dateString) ?: StatsResult(0, 0, 0, 0, 0, 0)
        
        val safeLeisureCount = stats.safeLeisureCount
        val safeLeisureCompleted = stats.safeLeisureCompleted
        val safeEffortCount = stats.safeEffortCount
        val safeEffortCompleted = stats.safeEffortCompleted

        val leisureScore = if (safeLeisureCount > 0) {
            (safeLeisureCompleted.toFloat() / safeLeisureCount) * 36.25f
        } else 0f
        
        val effortScore = if (safeEffortCount > 0) {
            (safeEffortCompleted.toFloat() / safeEffortCount) * 63.75f
        } else 0f
        
        val totalScore = Math.round(leisureScore + effortScore).toFloat().coerceIn(0f, 100f)

        val record = dao.getRecordByDate(dateString)
        if (record != null) {
            val updated = record.copy(dailyScore = totalScore)
            dao.update(updated)
            
            // Refrescar el estado del backup
            val logs = dao.getLogsByDateSnapshot(dateString).map { log ->
                ActivityLogItemData(log.id, log.name, log.date, log.category, log.isCompleted)
            }
            _uiState.value = _uiState.value.copy(
                backupLogs = logs,
                backupDayScore = totalScore
            )
            
            // Si el backup coincide con hoy, actualizar UI state actual
            if (dateString == getTodayDateString()) {
                currentRecord = updated
                _uiState.value = _uiState.value.copy(
                    dailyScore = totalScore,
                    leisureCount = safeLeisureCount,
                    effortCount = safeEffortCount
                )
            }
            
            recalculateStreak()
            recalculateAllStats()
        }
    }
}