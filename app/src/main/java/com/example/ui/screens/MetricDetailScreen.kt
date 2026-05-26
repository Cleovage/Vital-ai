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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FullHealthData
import com.example.ui.theme.*

import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.Color

@Composable
fun MetricDetailScreen(metricName: String, healthData: FullHealthData, viewModel: com.example.MainViewModel, onBack: () -> Unit) {
    val weeklyTrend = remember { androidx.compose.runtime.mutableStateOf<List<Float>>(emptyList()) }
    val userProfile by viewModel.userProfile.collectAsState()
    
    var showGoalEdit by remember { mutableStateOf(false) }
    var goalInputValue by remember { mutableStateOf("") }
    
    androidx.compose.runtime.LaunchedEffect(metricName) {
        weeklyTrend.value = viewModel.getWeeklyTrend(metricName)
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        .padding(24.dp)
                ) {
                    var value = "No Data"
                    var unit = ""
                    var goalText = ""
                    when (metricName.lowercase()) {
                        "steps" -> { value = healthData.today.steps.toString(); goalText = "Goal: ${userProfile.stepGoal}" }
                        "energy" -> { value = healthData.today.activeCalories.toInt().toString(); unit = "cal"; goalText = "Goal: ${userProfile.activeEnergyGoal} cal" }
                        "distance" -> { value = String.format("%.1f", healthData.today.distanceKm); unit = "km"; goalText = "Goal: ${userProfile.distanceGoalKm} km" }
                        "hydration" -> { value = String.format("%.1f", healthData.today.hydrationLiters); unit = "L"; goalText = "Goal: ${userProfile.hydrationGoalLiters} L" }
                        "heart_rate" -> { value = healthData.today.avgHR.toInt().toString(); unit = "bpm" }
                        "hrv" -> { value = healthData.today.hrv.toInt().toString(); unit = "ms" }
                        "spo2" -> { value = healthData.today.spo2.toInt().toString(); unit = "%" }
                        "stress" -> { value = healthData.today.stressLevel.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(value, color = TextPrimary, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                        if (unit.isNotEmpty()) {
                            Text(unit, color = TextSecondary, fontSize = 20.sp)
                        }
                        
                        if (goalText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(goalText, color = AccentTeal, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { 
                                    goalInputValue = ""
                                    showGoalEdit = true 
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = AccentTeal, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Text("Weekly Trends (Last 7 Days)", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(BackgroundSecondary)
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                if (weeklyTrend.value.isEmpty()) {
                    CircularProgressIndicator(color = AccentTeal, modifier = Modifier.align(Alignment.Center))
                } else if (weeklyTrend.value.all { it == 0f }) {
                    Text("No historical data available", color = TextSecondary, modifier = Modifier.align(Alignment.Center))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val days = listOf("6d", "5d", "4d", "3d", "2d", "1d", "Today")
                        val maxRaw = weeklyTrend.value.maxOrNull() ?: 0f
                        val maxVal = if (maxRaw > 0f) maxRaw else 1f
                        
                        days.forEachIndexed { idx, d ->
                            val v = weeklyTrend.value[idx]
                            val hRatio = (v / maxVal).coerceIn(0f, 1f)
                            
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                                Box(
                                    modifier = Modifier
                                        .width(16.dp)
                                        .weight(1f, fill=false)
                                        .fillMaxHeight(hRatio)
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

    if (showGoalEdit) {
        AlertDialog(
            onDismissRequest = { showGoalEdit = false },
            containerColor = BackgroundSecondary,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Update ${metricName.capitalize()} Goal") },
            text = {
                OutlinedTextField(
                    value = goalInputValue,
                    onValueChange = { goalInputValue = it },
                    label = { Text("New Goal") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val value = goalInputValue.toFloatOrNull()
                        if (value != null && value > 0) {
                            val newProfile = when (metricName.lowercase()) {
                                "steps" -> userProfile.copy(stepGoal = value.toInt())
                                "energy" -> userProfile.copy(activeEnergyGoal = value.toInt())
                                "distance" -> userProfile.copy(distanceGoalKm = value)
                                "hydration" -> userProfile.copy(hydrationGoalLiters = value)
                                else -> userProfile
                            }
                            viewModel.updateProfile(newProfile)
                        }
                        showGoalEdit = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalEdit = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
}
