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
        androidx.health.connect.client.HealthConnectClient.getSdkStatus(application) == 
        androidx.health.connect.client.HealthConnectClient.SDK_AVAILABLE
    )
    val isHealthConnectAvailable: StateFlow<Boolean> = _isHealthConnectAvailable

    val healthData = MutableStateFlow(
        FullHealthData(
            today = com.example.data.HealthSnapshot()
        )
    )

    fun checkPermissionsAndFetchData() {
        viewModelScope.launch {
            if (_isHealthConnectAvailable.value) {
                try {
                    if (healthConnectManager.hasAllPermissions()) {
                        val snapshot = healthConnectManager.readTodaySnapshot()
                        healthData.value = FullHealthData(today = snapshot)
                    } else {
                        healthData.value = FullHealthData() // Zeroes
                    }
                } catch (e: Exception) {
                    healthData.value = FullHealthData() // Fall back to zeros
                }
            }
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
