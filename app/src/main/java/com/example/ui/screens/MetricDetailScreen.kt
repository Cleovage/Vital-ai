package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FullHealthData
import com.example.ui.theme.*

@Composable
fun MetricDetailScreen(metricName: String, healthData: FullHealthData, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(top = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = metricName.capitalize(),
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }
        
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BackgroundSecondary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                var value = "No Data"
                var unit = ""
                when (metricName.lowercase()) {
                    "steps" -> { value = healthData.today.steps.toString() }
                    "energy" -> { value = healthData.today.activeCalories.toInt().toString(); unit = "cal" }
                    "distance" -> { value = String.format("%.1f", healthData.today.distanceKm); unit = "km" }
                    "hydration" -> { value = String.format("%.1f", healthData.today.hydrationLiters); unit = "L" }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = TextPrimary, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    if (unit.isNotEmpty()) {
                        Text(unit, color = TextSecondary, fontSize = 20.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Text("Weekly Trends", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dummy chart representation for now
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BackgroundSecondary)
                    .padding(16.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val heights = listOf(0.4f, 0.6f, 0.8f, 0.5f, 0.9f, 0.3f, 1.0f)
                    
                    days.forEachIndexed { idx, d ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(150.dp * heights[idx])
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(if (idx == 6) AccentBlue else AccentTeal)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(d, color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
