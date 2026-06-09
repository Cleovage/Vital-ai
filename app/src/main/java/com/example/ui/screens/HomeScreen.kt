package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.health.connect.client.PermissionController
import com.example.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FullHealthData
import com.example.ui.components.ActivityRing
import com.example.ui.components.MetricCard
import com.example.ui.theme.*

import androidx.compose.runtime.collectAsState

@Composable
fun HomeScreen(
    healthData: FullHealthData,
    viewModel: MainViewModel = viewModel(),
    onNavigateToCoach: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    val userProfile by viewModel.userProfile.collectAsState()
    
    val requestPermissions = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(viewModel.healthPermissions)) {
            viewModel.checkPermissionsAndFetchData()
        }
    }

    LaunchedEffect(Unit) {
        requestPermissions.launch(viewModel.healthPermissions)
        viewModel.checkPermissionsAndFetchData()
    }

    var addDialogMetric by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundPrimary)
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Daily Activity",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "You're doing great today!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Hero Concentric Rings (Samsung Health Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Steps Ring (Outer)
                ActivityRing(
                    progress = (healthData.today.steps / userProfile.stepGoal.toDouble()).toFloat().coerceIn(0f, 1f),
                    color = AccentTeal,
                    dimColor = AccentTeal.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxSize()
                )
                // Energy Ring (Middle)
                ActivityRing(
                    progress = (healthData.today.activeCalories / userProfile.activeEnergyGoal.toDouble()).toFloat().coerceIn(0f, 1f),
                    color = AccentGreen,
                    dimColor = AccentGreen.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxSize().padding(32.dp)
                )
                // Distance Ring (Inner)
                ActivityRing(
                    progress = (healthData.today.distanceKm / userProfile.distanceGoalKm).toFloat().coerceIn(0f, 1f),
                    color = AccentBlue,
                    dimColor = AccentBlue.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxSize().padding(64.dp)
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${healthData.today.steps}",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Steps",
                        color = AccentTeal,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Energy burned",
                        value = "${healthData.today.activeCalories.toInt()}",
                        unit = "cal",
                        progress = (healthData.today.activeCalories / userProfile.activeEnergyGoal.toDouble()).toFloat().coerceIn(0f, 1f),
                        badgeText = "${(userProfile.activeEnergyGoal - healthData.today.activeCalories).toInt().coerceAtLeast(0)} cal left",
                        progressColor = AccentGreen,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { addDialogMetric = "energy" },
                        onClick = { onNavigateToDetail("energy") }
                    )

                    MetricCard(
                        title = "Steps",
                        value = "${healthData.today.steps}",
                        unit = "",
                        progress = (healthData.today.steps / userProfile.stepGoal.toDouble()).toFloat().coerceIn(0f, 1f),
                        badgeText = "${(userProfile.stepGoal.toLong() - healthData.today.steps).coerceAtLeast(0L)} remaining",
                        progressColor = AccentTeal,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { addDialogMetric = "steps" },
                        onClick = { onNavigateToDetail("steps") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    MetricCard(
                        title = "Distance",
                        value = String.format("%.1f", healthData.today.distanceKm),
                        unit = "km",
                        progress = (healthData.today.distanceKm / userProfile.distanceGoalKm).toFloat().coerceIn(0f, 1f),
                        badgeText = "${String.format("%.1f", (userProfile.distanceGoalKm - healthData.today.distanceKm).coerceAtLeast(0.0))} km left",
                        progressColor = AccentBlue,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { addDialogMetric = "distance" },
                        onClick = { onNavigateToDetail("distance") }
                    )

                    MetricCard(
                        title = "Hydration",
                        value = String.format("%.1f", healthData.today.hydrationLiters),
                        unit = "L",
                        progress = (healthData.today.hydrationLiters / userProfile.hydrationGoalLiters).toFloat().coerceIn(0f, 1f),
                        badgeText = "${String.format("%.1f", (userProfile.hydrationGoalLiters - healthData.today.hydrationLiters).coerceAtLeast(0.0))} L to go",
                        progressColor = AccentBlue,
                        modifier = Modifier.weight(1f),
                        onQuickAdd = { addDialogMetric = "hydration" },
                        onClick = { onNavigateToDetail("hydration") }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        FloatingActionButton(
            onClick = { addDialogMetric = "steps" },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp),
            containerColor = AccentTeal,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Data", tint = Color.White)
        }
    }

    if (addDialogMetric != null) {
        AddDataDialog(
            initialMetric = addDialogMetric!!,
            onDismiss = { addDialogMetric = null },
            onSave = { metric, value ->
                viewModel.addManualData(metric, value)
                addDialogMetric = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDataDialog(initialMetric: String, onDismiss: () -> Unit, onSave: (String, Float) -> Unit) {
    var selectedMetric by remember { mutableStateOf(initialMetric) }
    var inputValue by remember { mutableStateOf("") }
    
    val metrics = listOf(
        "steps" to "Steps", 
        "energy" to "Calories", 
        "distance" to "Distance", 
        "hydration" to "Water",
        "heart_rate" to "Heart Rate",
        "hrv" to "HRV",
        "spo2" to "SpO2",
        "stress" to "Stress"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BackgroundSecondary,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text("Add Activity Data", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("Select Metric Type:")
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    metrics.subList(0, 3).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedMetric == key,
                            onClick = { selectedMetric = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    metrics.subList(3, 6).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedMetric == key,
                            onClick = { selectedMetric = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    metrics.subList(6, 8).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedMetric == key,
                            onClick = { selectedMetric = key },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Value", color = TextSecondary) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentTeal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val value = inputValue.toFloatOrNull()
                    if (value != null && value > 0) {
                        onSave(selectedMetric, value)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
