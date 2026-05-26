package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.PermissionController
import com.example.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FullHealthData
import com.example.ui.components.MetricCard
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    healthData: FullHealthData,
    viewModel: MainViewModel = viewModel(),
    onNavigateToCoach: () -> Unit
) {
    val context = LocalContext.current
    val requestPermissionActivityContract = PermissionController.createRequestPermissionResultContract()
    
    val requestPermissions = rememberLauncherForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(viewModel.healthPermissions)) {
            viewModel.checkPermissionsAndFetchData()
        }
    }

    LaunchedEffect(Unit) {
        requestPermissions.launch(viewModel.healthPermissions)
        viewModel.checkPermissionsAndFetchData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
            .padding(top = 40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Text(
                    text = "Quick Glance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Grid Layout using pairs since we don't have LazyVerticalGrid imported directly (and to be safe with simple layouts)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Energy burned",
                    value = "${healthData.today.activeCalories.toInt()}",
                    unit = "cal",
                    progress = (healthData.today.activeCalories / 2000.0).toFloat().coerceIn(0f, 1f),
                    badgeText = "${(2000 - healthData.today.activeCalories).toInt().coerceAtLeast(0)} cal left",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Steps",
                    value = "${healthData.today.steps}",
                    unit = "",
                    progress = (healthData.today.steps / 10000.0).toFloat().coerceIn(0f, 1f),
                    badgeText = "${healthData.today.steps} daily total",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricCard(
                    title = "Distance",
                    value = String.format("%.1f", healthData.today.distanceKm),
                    unit = "km",
                    progress = (healthData.today.distanceKm / 5.0).toFloat().coerceIn(0f, 1f),
                    badgeText = "${String.format("%.1f", (5.0 - healthData.today.distanceKm).coerceAtLeast(0.0))} km left",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Hydration",
                    value = String.format("%.1f", healthData.today.hydrationLiters),
                    unit = "L",
                    progress = (healthData.today.hydrationLiters / 2.5).toFloat().coerceIn(0f, 1f),
                    badgeText = "${String.format("%.1f", (2.5 - healthData.today.hydrationLiters).coerceAtLeast(0.0))} L to go",
                    progressColor = AccentTeal,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
