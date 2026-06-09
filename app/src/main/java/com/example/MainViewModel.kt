package com.example

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.FullHealthData
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.Part
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MainViewModel(application: Application) : androidx.lifecycle.AndroidViewModel(application) {
    
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "vitaai-db"
    ).fallbackToDestructiveMigration().build()

    val chatMessages: StateFlow<List<ChatMessage>> = db.chatDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userProfile: StateFlow<com.example.data.UserProfile> = db.userProfileDao().getProfile()
        .map { it ?: com.example.data.UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.UserProfile())

    fun updateProfile(profile: com.example.data.UserProfile) {
        viewModelScope.launch { db.userProfileDao().upsertProfile(profile) }
    }

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    private val healthConnectManager = com.example.data.HealthConnectManager(application)
    
    val healthPermissions = healthConnectManager.permissions

    private val _isHealthConnectAvailable = MutableStateFlow(
        try {
            androidx.health.connect.client.HealthConnectClient.getSdkStatus(application) == 
            androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE
        } catch (e: Exception) {
            false
        }
    )
    val isHealthConnectAvailable: StateFlow<Boolean> = _isHealthConnectAvailable

    val healthData = MutableStateFlow(
        FullHealthData(
            today = com.example.data.HealthSnapshot()
        )
    )

    private var manualSteps = 0
    private var manualCalories = 0.0
    private var manualDistance = 0.0
    private var manualHydration = 0.0
    private var manualAvgHR = 0.0
    private var manualHRV = 0.0
    private var manualSpO2 = 0.0
    private var manualStress = ""

    private fun getMockTodaySnapshot(): com.example.data.HealthSnapshot {
        val stepsValue = 7900 + manualSteps
        val stepsGoal = 10000.0
        val caloriesValue = 460.0 + manualCalories
        val hrvValue = if (manualHRV > 0.0) manualHRV else 48.0
        return com.example.data.HealthSnapshot(
            steps = stepsValue,
            stepGoalPercent = ((stepsValue / stepsGoal) * 100).toInt(),
            activeCalories = caloriesValue,
            totalCalories = caloriesValue + 1600.0,
            distanceKm = 5.5 + manualDistance,
            restingHR = if (manualAvgHR > 0.0) (manualAvgHR - 8.0).coerceAtLeast(40.0) else 62.0,
            maxHR = if (manualAvgHR > 0.0) (manualAvgHR + 30.0).coerceAtMost(200.0) else 145.0,
            avgHR = if (manualAvgHR > 0.0) manualAvgHR else 70.0,
            hrv = hrvValue,
            spo2 = if (manualSpO2 > 0.0) manualSpO2 else 98.4,
            stressLevel = if (manualStress.isNotEmpty()) {
                manualStress
            } else if (hrvValue < 30.0) {
                "high"
            } else {
                "moderate"
            },
            hydrationLiters = 2.4 + manualHydration
        )
    }

    private suspend fun refreshSnapshot() {
        if (_isHealthConnectAvailable.value && healthConnectManager.hasAllPermissions()) {
            try {
                val snapshot = healthConnectManager.readTodaySnapshot()
                // Merge with manual vitals which can only be stored in-memory
                healthData.value = FullHealthData(
                    today = snapshot.copy(
                        avgHR = if (manualAvgHR > 0.0) manualAvgHR else snapshot.avgHR,
                        restingHR = if (manualAvgHR > 0.0) (manualAvgHR - 8.0).coerceAtLeast(40.0) else snapshot.restingHR,
                        maxHR = if (manualAvgHR > 0.0) (manualAvgHR + 30.0).coerceAtMost(200.0) else snapshot.maxHR,
                        hrv = if (manualHRV > 0.0) manualHRV else snapshot.hrv,
                        spo2 = if (manualSpO2 > 0.0) manualSpO2 else snapshot.spo2,
                        stressLevel = if (manualStress.isNotEmpty()) {
                            manualStress
                        } else if (manualHRV > 0.0) {
                            if (manualHRV < 30.0) "high" else "moderate"
                        } else snapshot.stressLevel
                    )
                )
            } catch (e: Exception) {
                healthData.value = FullHealthData(today = getMockTodaySnapshot())
            }
        } else {
            healthData.value = FullHealthData(today = getMockTodaySnapshot())
        }
    }

    fun checkPermissionsAndFetchData() {
        viewModelScope.launch {
            refreshSnapshot()
        }
    }

    suspend fun getWeeklyTrend(metricName: String): List<Float> {
        val trend = if (_isHealthConnectAvailable.value && healthConnectManager.hasAllPermissions()) {
            try {
                healthConnectManager.readWeeklyTrend(metricName)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val isTrendEmpty = trend.isEmpty() || trend.all { it == 0f }
        val finalTrend = if (isTrendEmpty) {
            val key = metricName.lowercase()
            when (key) {
                "steps" -> listOf(6200f, 7400f, 5100f, 8200f, 9100f, 6800f, healthData.value.today.steps.toFloat())
                "energy" -> listOf(320f, 450f, 290f, 520f, 610f, 380f, healthData.value.today.activeCalories.toFloat())
                "distance" -> listOf(4.5f, 5.2f, 3.8f, 6.1f, 6.8f, 4.9f, healthData.value.today.distanceKm.toFloat())
                "hydration" -> listOf(1.8f, 2.2f, 1.5f, 2.5f, 2.8f, 2.0f, healthData.value.today.hydrationLiters.toFloat())
                "heart_rate" -> listOf(72f, 75f, 68f, 74f, 71f, 73f, healthData.value.today.avgHR.toFloat())
                "hrv" -> listOf(42f, 45f, 38f, 47f, 52f, 44f, healthData.value.today.hrv.toFloat())
                "spo2" -> listOf(98.5f, 98.2f, 99.0f, 97.8f, 98.6f, 98.9f, healthData.value.today.spo2.toFloat())
                "stress" -> listOf(22f, 35f, 40f, 18f, 15f, 28f, if (healthData.value.today.stressLevel == "high") 75f else if (healthData.value.today.stressLevel == "moderate") 45f else 25f)
                else -> List(7) { 0f }
            }
        } else {
            val mutableTrend = trend.toMutableList()
            if (mutableTrend.size >= 7) {
                val key = metricName.lowercase()
                mutableTrend[mutableTrend.size - 1] = when (key) {
                    "steps" -> healthData.value.today.steps.toFloat()
                    "energy" -> healthData.value.today.activeCalories.toFloat()
                    "distance" -> healthData.value.today.distanceKm.toFloat()
                    "hydration" -> healthData.value.today.hydrationLiters.toFloat()
                    "heart_rate" -> healthData.value.today.avgHR.toFloat()
                    "hrv" -> healthData.value.today.hrv.toFloat()
                    "spo2" -> healthData.value.today.spo2.toFloat()
                    "stress" -> if (healthData.value.today.stressLevel == "high") 75f else if (healthData.value.today.stressLevel == "moderate") 45f else 25f
                    else -> mutableTrend.last()
                }
            }
            mutableTrend
        }
        return finalTrend
    }

    fun addManualData(metricName: String, value: Float) {
        viewModelScope.launch {
            val key = metricName.lowercase()
            if (_isHealthConnectAvailable.value && healthConnectManager.hasAllPermissions()) {
                try {
                    when (key) {
                        "steps" -> healthConnectManager.writeSteps(value.toLong())
                        "energy" -> healthConnectManager.writeActiveCalories(value.toDouble())
                        "distance" -> healthConnectManager.writeDistance(value.toDouble())
                        "hydration" -> healthConnectManager.writeHydration(value.toDouble())
                        "heart_rate" -> manualAvgHR = value.toDouble()
                        "hrv" -> manualHRV = value.toDouble()
                        "spo2" -> manualSpO2 = value.toDouble()
                        "stress" -> {
                            manualStress = when {
                                value > 70f -> "high"
                                value > 30f -> "moderate"
                                else -> "low"
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Fallback to local storage on error
                    when (key) {
                        "steps" -> manualSteps += value.toInt()
                        "energy" -> manualCalories += value.toDouble()
                        "distance" -> manualDistance += value.toDouble()
                        "hydration" -> manualHydration += value.toDouble()
                        "heart_rate" -> manualAvgHR = value.toDouble()
                        "hrv" -> manualHRV = value.toDouble()
                        "spo2" -> manualSpO2 = value.toDouble()
                        "stress" -> {
                            manualStress = when {
                                value > 70f -> "high"
                                value > 30f -> "moderate"
                                else -> "low"
                            }
                        }
                    }
                }
            } else {
                when (key) {
                    "steps" -> manualSteps += value.toInt()
                    "energy" -> manualCalories += value.toDouble()
                    "distance" -> manualDistance += value.toDouble()
                    "hydration" -> manualHydration += value.toDouble()
                    "heart_rate" -> manualAvgHR = value.toDouble()
                    "hrv" -> manualHRV = value.toDouble()
                    "spo2" -> manualSpO2 = value.toDouble()
                    "stress" -> {
                        manualStress = when {
                            value > 70f -> "high"
                            value > 30f -> "moderate"
                            else -> "low"
                        }
                    }
                }
            }
            refreshSnapshot()
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val userMsg = ChatMessage(role = "user", content = text)
            db.chatDao().insertMessage(userMsg)
            _isTyping.value = true

            // Send to Gemini
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                val historyContents = chatMessages.value.map {
                    Content(parts = listOf(Part(text = it.content)), role = if (it.role == "user") "user" else "model")
                }.toMutableList()
                historyContents.add(Content(parts = listOf(Part(text = text)), role = "user"))
                
                val currentHealth = healthData.value.today
                val systemPrompt = """
                    You are VitaAI Coach, a warm, knowledgeable personal health assistant. Keep responses clear and formatted in markdown. Focus on fitness analysis.
                    
                    Today's Data:
                    Steps: ${currentHealth.steps}
                    Calories: ${currentHealth.activeCalories} kcal active / ${currentHealth.totalCalories} kcal total
                    Heart Rate: Avg ${currentHealth.avgHR} bpm (Max: ${currentHealth.maxHR} bpm, Min: ${currentHealth.restingHR} bpm)
                    HRV: ${currentHealth.hrv} ms
                    Hydration: ${currentHealth.hydrationLiters} L
                    SpO2: ${currentHealth.spo2}%
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = historyContents,
                    systemInstruction = Content(
                        parts = listOf(Part(text = systemPrompt))
                    )
                )
                
                val responseBody = RetrofitClient.service.generateContentStream(apiKey, request)
                var accumulatedResponse = ""
                
                withContext(Dispatchers.IO) {
                    responseBody.byteStream().bufferedReader().use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            try {
                                val chunk = Json.parseToJsonElement(line!!).jsonObject
                                val textPart = chunk["candidates"]?.jsonArray
                                    ?.getOrNull(0)?.jsonObject
                                    ?.get("content")?.jsonObject
                                    ?.get("parts")?.jsonArray
                                    ?.getOrNull(0)?.jsonObject
                                    ?.get("text")?.jsonPrimitive?.content
                                if (textPart != null) {
                                    accumulatedResponse += textPart
                                }
                            } catch (e: Exception) {
                                // Ignore non-json or incomplete chunks if any
                            }
                        }
                    }
                }
                
                db.chatDao().insertMessage(ChatMessage(role = "assistant", content = accumulatedResponse))
                
            } catch (e: Exception) {
                db.chatDao().insertMessage(ChatMessage(role = "assistant", content = "I'm having trouble connecting right now. Let's try again later!"))
            } finally {
                _isTyping.value = false
            }
        }
    }
}
