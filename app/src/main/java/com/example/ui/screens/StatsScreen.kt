package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FullHealthData
import com.example.ui.components.MetricCard
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.TextPrimary

@Composable
fun StatsScreen(healthData: FullHealthData, onNavigateToDetail: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Detailed Analytics",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Steps",
                    value = "${healthData.today.steps}",
                    unit = "",
                    progress = (healthData.today.steps / 10000.0).toFloat().coerceIn(0f, 1f),
                    badgeText = "Daily Total",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("steps") }
                )

                MetricCard(
                    title = "Active Energy",
                    value = "${healthData.today.activeCalories.toInt()}",
                    unit = "cal",
                    progress = (healthData.today.activeCalories / 2000.0).toFloat().coerceIn(0f, 1f),
                    badgeText = "Daily Total",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("energy") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Distance",
                    value = String.format("%.1f", healthData.today.distanceKm),
                    unit = "km",
                    progress = (healthData.today.distanceKm / 5.0).toFloat().coerceIn(0f, 1f),
                    badgeText = "Daily Total",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("distance") }
                )

                MetricCard(
                    title = "Hydration",
                    value = String.format("%.1f", healthData.today.hydrationLiters),
                    unit = "L",
                    progress = (healthData.today.hydrationLiters / 2.5).toFloat().coerceIn(0f, 1f),
                    badgeText = "Daily Total",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("hydration") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Heart & Vitals", color = TextPrimary, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Avg Heart Rate",
                    value = if (healthData.today.avgHR > 0) "${healthData.today.avgHR.toInt()}" else "No data",
                    unit = if (healthData.today.avgHR > 0) "bpm" else "",
                    progress = if (healthData.today.avgHR > 0) 0.6f else 0f,
                    badgeText = "Resting: ${healthData.today.restingHR.toInt()}",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("heart_rate") }
                )

                MetricCard(
                    title = "HRV",
                    value = if (healthData.today.hrv > 0) "${healthData.today.hrv.toInt()}" else "No data",
                    unit = if (healthData.today.hrv > 0) "ms" else "",
                    progress = if (healthData.today.hrv > 0) 0.5f else 0f,
                    badgeText = "Daily Avg",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("hrv") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "SpO2",
                    value = if (healthData.today.spo2 > 0) String.format("%.1f", healthData.today.spo2) else "No data",
                    unit = if (healthData.today.spo2 > 0) "%" else "",
                    progress = if (healthData.today.spo2 > 0) (healthData.today.spo2 / 100.0).toFloat() else 0f,
                    badgeText = "Daily Avg",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("spo2") }
                )

                MetricCard(
                    title = "Stress Level",
                    value = healthData.today.stressLevel.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.ifEmpty { "No data" },
                    unit = "",
                    progress = 0f,
                    badgeText = "Status",
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToDetail("stress") }
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
