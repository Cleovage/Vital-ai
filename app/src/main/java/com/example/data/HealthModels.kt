package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class HealthSnapshot(
    val steps: Int = 0,
    val stepGoalPercent: Int = 0,
    val activeCalories: Double = 0.0,
    val totalCalories: Double = 0.0,
    val distanceKm: Double = 0.0,
    val restingHR: Double = 0.0,
    val maxHR: Double = 0.0,
    val avgHR: Double = 0.0,
    val hrv: Double = 0.0,
    val spo2: Double = 0.0,
    val stressLevel: String = "unknown",
    val hydrationLiters: Double = 0.0
)

@Serializable
data class SleepSession(
    val durationMinutes: Int = 0,
    val score: Int = 0,
    val deepSleepMinutes: Int = 0,
    val remSleepMinutes: Int = 0,
    val lightSleepMinutes: Int = 0,
    val awakeMinutes: Int = 0,
    val bedtime: String = "",
    val wakeTime: String = "",
    val efficiency: Int = 0
)

@Serializable
data class FullHealthData(
    val today: HealthSnapshot = HealthSnapshot(),
    val sleep: SleepSession = SleepSession()
)
