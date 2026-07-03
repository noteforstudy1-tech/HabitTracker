package com.habittracker.app.data.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.habittracker.app.data.model.Habit
import com.habittracker.app.data.model.HabitCompletion
import com.habittracker.app.data.model.WellnessEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupHelper {

    fun exportDataToJson(
        habits: List<Habit>,
        completions: List<HabitCompletion>,
        wellness: List<WellnessEntry>
    ): String {
        val root = JSONObject()

        // 1. Export Habits
        val habitsArray = JSONArray()
        habits.forEach { habit ->
            val obj = JSONObject().apply {
                put("id", habit.id)
                put("name", habit.name)
                put("colorHex", habit.colorHex)
                put("createdAt", habit.createdAt)
                put("frequencyType", habit.frequencyType)
                put("customDays", habit.customDays)
            }
            habitsArray.put(obj)
        }
        root.put("habits", habitsArray)

        // 2. Export Completions
        val completionsArray = JSONArray()
        completions.forEach { completion ->
            val obj = JSONObject().apply {
                put("habitId", completion.habitId)
                put("dateEpochDay", completion.dateEpochDay)
                put("isCompleted", completion.isCompleted)
            }
            completionsArray.put(obj)
        }
        root.put("completions", completionsArray)

        // 3. Export Wellness
        val wellnessArray = JSONArray()
        wellness.forEach { entry ->
            val obj = JSONObject().apply {
                put("dateEpochDay", entry.dateEpochDay)
                put("moodIndex", entry.moodIndex)
                put("sleepHours", entry.sleepHours)
            }
            wellnessArray.put(obj)
        }
        root.put("wellness", wellnessArray)

        return root.toString(2) // Pretty print JSON with indent=2
    }

    fun parseImportedData(jsonString: String): ImportedData? {
        return try {
            val root = JSONObject(jsonString)

            val habits = mutableListOf<Habit>()
            val habitsArray = root.optJSONArray("habits")
            if (habitsArray != null) {
                for (i in 0 until habitsArray.length()) {
                    val obj = habitsArray.getJSONObject(i)
                    habits.add(
                        Habit(
                            id = obj.optLong("id", 0),
                            name = obj.optString("name", ""),
                            colorHex = obj.optString("colorHex", "#7C3AED"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                            frequencyType = obj.optString("frequencyType", "DAILY"),
                            customDays = obj.optString("customDays", "1,2,3,4,5,6,7")
                        )
                    )
                }
            }

            val completions = mutableListOf<HabitCompletion>()
            val completionsArray = root.optJSONArray("completions")
            if (completionsArray != null) {
                for (i in 0 until completionsArray.length()) {
                    val obj = completionsArray.getJSONObject(i)
                    completions.add(
                        HabitCompletion(
                            habitId = obj.optLong("habitId", 0),
                            dateEpochDay = obj.optLong("dateEpochDay", 0),
                            isCompleted = obj.optBoolean("isCompleted", true)
                        )
                    )
                }
            }

            val wellness = mutableListOf<WellnessEntry>()
            val wellnessArray = root.optJSONArray("wellness")
            if (wellnessArray != null) {
                for (i in 0 until wellnessArray.length()) {
                    val obj = wellnessArray.getJSONObject(i)
                    wellness.add(
                        WellnessEntry(
                            dateEpochDay = obj.optLong("dateEpochDay", 0),
                            moodIndex = obj.optInt("moodIndex", 2),
                            sleepHours = obj.optDouble("sleepHours", 7.0).toFloat()
                        )
                    )
                }
            }

            ImportedData(habits, completions, wellness)
        } catch (e: Exception) {
            Log.e("BackupHelper", "Error parsing backup data", e)
            null
        }
    }

    fun readTextFromUri(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }
}

data class ImportedData(
    val habits: List<Habit>,
    val completions: List<HabitCompletion>,
    val wellness: List<WellnessEntry>
)
