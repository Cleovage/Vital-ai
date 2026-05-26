package com.example.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getWritePermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(HydrationRecord::class),
        HealthPermission.getWritePermission(HydrationRecord::class),
    )

    suspend fun hasAllPermissions(): Boolean {
        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readTodaySnapshot(): HealthSnapshot {
        val now = ZonedDateTime.now()
        val startOfDay = now.truncatedTo(ChronoUnit.DAYS).toInstant()
        val endOfDay = now.plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant()
        val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)

        // Steps
        val stepsResponse = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = timeRange
            )
        )
        val steps = stepsResponse[StepsRecord.COUNT_TOTAL] ?: 0L

        // Calories
        val caloriesResponse = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL),
                timeRangeFilter = timeRange
            )
        )
        val calories = caloriesResponse[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0

        // Distance
        val distanceResponse = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
                timeRangeFilter = timeRange
            )
        )
        val distance = distanceResponse[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0

        // Heart Rate
        val hrResponse = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(
                    HeartRateRecord.BPM_AVG, 
                    HeartRateRecord.BPM_MAX, 
                    HeartRateRecord.BPM_MIN
                ),
                timeRangeFilter = timeRange
            )
        )
        val avgHr = hrResponse[HeartRateRecord.BPM_AVG] ?: 0L
        val minHr = hrResponse[HeartRateRecord.BPM_MIN] ?: 0L
        val maxHr = hrResponse[HeartRateRecord.BPM_MAX] ?: 0L

        // HRV
        val hrvResponse = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateVariabilityRmssdRecord::class,
                timeRangeFilter = timeRange,
                pageSize = 1
            )
        )
        val hrv = hrvResponse.records.firstOrNull()?.heartRateVariabilityMillis ?: 0.0

        // SpO2
        val spo2Response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = OxygenSaturationRecord::class,
                timeRangeFilter = timeRange,
                pageSize = 1
            )
        )
        val spo2 = spo2Response.records.firstOrNull()?.percentage?.value ?: 0.0

        // Hydration
        val hydrationResponse = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(HydrationRecord.VOLUME_TOTAL),
                timeRangeFilter = timeRange
            )
        )
        val hydration = hydrationResponse[HydrationRecord.VOLUME_TOTAL]?.inLiters ?: 0.0

        return HealthSnapshot(
            steps = steps.toInt(),
            stepGoalPercent = (steps / 8000.0 * 100).toInt(),
            activeCalories = calories,
            totalCalories = calories + 1600.0, // rough BMR estimate base
            distanceKm = distance,
            restingHR = minHr.toDouble(),
            maxHR = maxHr.toDouble(),
            avgHR = avgHr.toDouble(),
            hrv = hrv,
            spo2 = spo2,
            stressLevel = if (hrv > 0 && hrv < 30) "high" else if (hrv >= 30) "moderate" else "unknown",
            hydrationLiters = hydration
        )
    }

    suspend fun readWeeklyTrend(metricName: String): List<Float> {
        val now = ZonedDateTime.now()
        val results = mutableListOf<Float>()
        
        // Let's just fetch the last 7 days
        for (i in 6 downTo 0) {
            val startOfDay = now.minusDays(i.toLong()).truncatedTo(ChronoUnit.DAYS).toInstant()
            val endOfDay = now.minusDays(i.toLong()).plusDays(1).truncatedTo(ChronoUnit.DAYS).toInstant()
            val timeRange = TimeRangeFilter.between(startOfDay, endOfDay)
            
            try {
                val value = when(metricName.lowercase()) {
                    "steps" -> {
                        val r = healthConnectClient.aggregate(AggregateRequest(setOf(StepsRecord.COUNT_TOTAL), timeRange))
                        (r[StepsRecord.COUNT_TOTAL] ?: 0L).toFloat()
                    }
                    "energy" -> {
                        val r = healthConnectClient.aggregate(AggregateRequest(setOf(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL), timeRange))
                        (r[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0).toFloat()
                    }
                    "distance" -> {
                        val r = healthConnectClient.aggregate(AggregateRequest(setOf(DistanceRecord.DISTANCE_TOTAL), timeRange))
                        (r[DistanceRecord.DISTANCE_TOTAL]?.inKilometers ?: 0.0).toFloat()
                    }
                    "hydration" -> {
                        val r = healthConnectClient.aggregate(AggregateRequest(setOf(HydrationRecord.VOLUME_TOTAL), timeRange))
                        (r[HydrationRecord.VOLUME_TOTAL]?.inLiters ?: 0.0).toFloat()
                    }
                    "heart_rate" -> {
                        val r = healthConnectClient.aggregate(AggregateRequest(setOf(HeartRateRecord.BPM_AVG), timeRange))
                        (r[HeartRateRecord.BPM_AVG] ?: 0L).toFloat()
                    }
                    "hrv" -> {
                        val r = healthConnectClient.readRecords(ReadRecordsRequest(HeartRateVariabilityRmssdRecord::class, timeRange, pageSize = 1))
                        (r.records.firstOrNull()?.heartRateVariabilityMillis ?: 0.0).toFloat()
                    }
                    "spo2" -> {
                        val r = healthConnectClient.readRecords(ReadRecordsRequest(OxygenSaturationRecord::class, timeRange, pageSize = 1))
                        (r.records.firstOrNull()?.percentage?.value ?: 0.0).toFloat()
                    }
                    else -> 0f
                }
                results.add(value)
            } catch(e: Exception) {
                results.add(0f)
            }
        }
        return results
    }

    suspend fun writeSteps(count: Long) {
        try {
            val now = ZonedDateTime.now()
            val start = now.minusMinutes(1).toInstant()
            val end = now.toInstant()
            val record = StepsRecord(
                count = count,
                startTime = start,
                endTime = end,
                startZoneOffset = now.offset,
                endZoneOffset = now.offset
            )
            healthConnectClient.insertRecords(listOf(record))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun writeHydration(liters: Double) {
        try {
            val now = ZonedDateTime.now()
            val start = now.minusMinutes(1).toInstant()
            val end = now.toInstant()
            val record = HydrationRecord(
                volume = androidx.health.connect.client.units.Volume.liters(liters),
                startTime = start,
                endTime = end,
                startZoneOffset = now.offset,
                endZoneOffset = now.offset
            )
            healthConnectClient.insertRecords(listOf(record))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun writeActiveCalories(kcal: Double) {
        try {
            val now = ZonedDateTime.now()
            val start = now.minusMinutes(1).toInstant()
            val end = now.toInstant()
            val record = ActiveCaloriesBurnedRecord(
                energy = androidx.health.connect.client.units.Energy.kilocalories(kcal),
                startTime = start,
                endTime = end,
                startZoneOffset = now.offset,
                endZoneOffset = now.offset
            )
            healthConnectClient.insertRecords(listOf(record))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun writeDistance(km: Double) {
        try {
            val now = ZonedDateTime.now()
            val start = now.minusMinutes(1).toInstant()
            val end = now.toInstant()
            val record = DistanceRecord(
                distance = androidx.health.connect.client.units.Length.kilometers(km),
                startTime = start,
                endTime = end,
                startZoneOffset = now.offset,
                endZoneOffset = now.offset
            )
            healthConnectClient.insertRecords(listOf(record))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
