package com.example.tioro

import android.content.Context
import com.example.tioro.data.local.ActivityLog
import com.example.tioro.data.local.ActivityTemplate
import com.example.tioro.data.local.DailyRecord
import com.example.tioro.data.local.DailyRecordDao
import com.example.tioro.data.local.UserPreferencesRepository
import com.example.tioro.data.local.WorkBlockObjective
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

object TioroBackupExporter {

    suspend fun exportToJson(
        context: Context,
        dao: DailyRecordDao,
        userPrefs: UserPreferencesRepository
    ): String {
        val root = JSONObject()

        val userName = userPrefs.userNameFlow.first() ?: ""
        root.put("userName", userName)

        // 1. Daily Records
        val dailyRecords = dao.getAllDailyRecordsSnapshot()
        val recordsArray = JSONArray()
        dailyRecords.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("date", r.date)
                put("leisureCount", r.leisureCount)
                put("effortCount", r.effortCount)
                put("projectPoints", r.projectPoints.toDouble())
                put("baseScore", r.baseScore.toDouble())
                put("bonusScore", r.bonusScore.toDouble())
                put("dailyScore", r.dailyScore.toDouble())
            }
            recordsArray.put(obj)
        }
        root.put("daily_records", recordsArray)

        // 2. Activity Logs
        val logs = dao.getAllActivityLogsSnapshot()
        val logsArray = JSONArray()
        logs.forEach { l ->
            val obj = JSONObject().apply {
                put("id", l.id)
                put("dailyRecordId", l.dailyRecordId)
                put("templateId", l.templateId ?: JSONObject.NULL)
                put("name", l.name)
                put("category", l.category)
                put("date", l.date)
                put("isCompleted", l.isCompleted)
            }
            logsArray.put(obj)
        }
        root.put("activity_logs", logsArray)

        // 3. Activity Templates
        val templates = dao.getAllTemplatesSnapshot()
        val templatesArray = JSONArray()
        templates.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("category", t.category)
                put("recurrenceDays", t.recurrenceDays)
            }
            templatesArray.put(obj)
        }
        root.put("activity_templates", templatesArray)

        // 4. Work Block Objectives
        val objectives = dao.getAllObjectivesSnapshot()
        val objectivesArray = JSONArray()
        objectives.forEach { o ->
            val obj = JSONObject().apply {
                put("id", o.id)
                put("weekStartDate", o.weekStartDate)
                put("blockTitle", o.blockTitle)
                put("objectiveName", o.objectiveName)
                put("isCompleted", o.isCompleted)
                put("completedDate", o.completedDate ?: JSONObject.NULL)
            }
            objectivesArray.put(obj)
        }
        root.put("work_block_objectives", objectivesArray)

        return root.toString(2)
    }

    suspend fun importFromJson(
        context: Context,
        dao: DailyRecordDao,
        userPrefs: UserPreferencesRepository,
        jsonString: String
    ): Boolean {
        return try {
            val root = JSONObject(jsonString)

            val userName = root.optString("userName", "")
            val recordsArray = root.optJSONArray("daily_records") ?: JSONArray()
            val logsArray = root.optJSONArray("activity_logs") ?: JSONArray()
            val templatesArray = root.optJSONArray("activity_templates") ?: JSONArray()
            val objectivesArray = root.optJSONArray("work_block_objectives") ?: JSONArray()

            // 1. Clear existing tables respecting Foreign Keys
            dao.clearAllActivityLogs()
            dao.clearAllDailyRecords()
            dao.clearAllTemplates()
            dao.clearAllObjectives()

            // 2. Restore User Preferences
            if (userName.isNotBlank()) {
                userPrefs.saveUserName(userName)
            }

            // 3. Restore Daily Records
            val recordsList = mutableListOf<DailyRecord>()
            for (i in 0 until recordsArray.length()) {
                val obj = recordsArray.getJSONObject(i)
                recordsList.add(
                    DailyRecord(
                        id = obj.getInt("id"),
                        date = obj.getString("date"),
                        leisureCount = obj.optInt("leisureCount", 0),
                        effortCount = obj.optInt("effortCount", 0),
                        projectPoints = obj.optDouble("projectPoints", 0.0).toFloat(),
                        baseScore = obj.optDouble("baseScore", 0.0).toFloat(),
                        bonusScore = obj.optDouble("bonusScore", 0.0).toFloat(),
                        dailyScore = obj.optDouble("dailyScore", 0.0).toFloat()
                    )
                )
            }
            dao.insertAllDailyRecords(recordsList)

            // 4. Restore Activity Logs
            val logsList = mutableListOf<ActivityLog>()
            for (i in 0 until logsArray.length()) {
                val obj = logsArray.getJSONObject(i)
                val templateId = if (obj.isNull("templateId")) null else obj.getInt("templateId")
                logsList.add(
                    ActivityLog(
                        id = obj.getInt("id"),
                        dailyRecordId = obj.getInt("dailyRecordId"),
                        templateId = templateId,
                        name = obj.getString("name"),
                        category = obj.getString("category"),
                        date = obj.getString("date"),
                        isCompleted = obj.getBoolean("isCompleted")
                    )
                )
            }
            dao.insertAllActivityLogs(logsList)

            // 5. Restore Activity Templates
            val templatesList = mutableListOf<ActivityTemplate>()
            for (i in 0 until templatesArray.length()) {
                val obj = templatesArray.getJSONObject(i)
                templatesList.add(
                    ActivityTemplate(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        category = obj.getString("category"),
                        recurrenceDays = obj.getString("recurrenceDays")
                    )
                )
            }
            dao.insertAllTemplates(templatesList)

            // 6. Restore Work Block Objectives
            val objectivesList = mutableListOf<WorkBlockObjective>()
            for (i in 0 until objectivesArray.length()) {
                val obj = objectivesArray.getJSONObject(i)
                val completedDate = if (obj.isNull("completedDate")) null else obj.getString("completedDate")
                objectivesList.add(
                    WorkBlockObjective(
                        id = obj.getInt("id"),
                        weekStartDate = obj.getString("weekStartDate"),
                        blockTitle = obj.getString("blockTitle"),
                        objectiveName = obj.getString("objectiveName"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        completedDate = completedDate
                    )
                )
            }
            dao.insertAllObjectives(objectivesList)

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
