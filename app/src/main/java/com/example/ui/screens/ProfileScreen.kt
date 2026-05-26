package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.data.UserProfile
import com.example.ui.theme.*

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val profile by viewModel.userProfile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        if (!profile.isLoggedIn) {
            Text(
                text = "Welcome",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(BackgroundSecondary)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Sign in to track your data across devices securely.",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.updateProfile(profile.copy(isLoggedIn = true, name = "John Doe", email = "john.doe@gmail.com")) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign in with Google", color = Color.White)
                    }
                }
            }
        } else {
            // Logged in UI
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp).background(AccentGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(profile.name.takeIf { it.isNotEmpty() }?.substring(0, 1) ?: "U", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(profile.name, color = TextPrimary, style = MaterialTheme.typography.titleLarge)
                    Text(profile.email, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // Customization Options
            var isEditing by remember { mutableStateOf(false) }
            
            if (isEditing) {
                EditProfileForm(profile = profile, onSave = { updatedProfile ->
                    viewModel.updateProfile(updatedProfile)
                    isEditing = false
                }, onCancel = { isEditing = false })
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(BackgroundSecondary)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Body Composition", color = AccentTeal, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { isEditing = true }) {
                                Text("Edit", color = AccentBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Gender", profile.gender.ifEmpty { "Not set" })
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Height", if (profile.heightCm > 0) "${profile.heightCm} cm" else "Not set")
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Weight", if (profile.weightKg > 0) "${profile.weightKg} kg" else "Not set")
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(BackgroundSecondary)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Daily Goals", color = AccentTeal, style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { isEditing = true }) {
                                Text("Edit", color = AccentBlue)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Divider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Steps", "${profile.stepGoal}")
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Active Energy", "${profile.activeEnergyGoal} cal")
                        Spacer(modifier = Modifier.height(16.dp))
                        DetailRow("Sleep", "${profile.sleepGoalHours} hrs")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { viewModel.updateProfile(profile.copy(isLoggedIn = false)) },
                    colors = ButtonDefaults.buttonColors(containerColor = BackgroundTertiary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Out", color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary)
        Text(value, color = TextPrimary)
    }
}

@Composable
fun EditProfileForm(profile: UserProfile, onSave: (UserProfile) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(profile.name) }
    var height by remember { mutableStateOf(profile.heightCm.toString()) }
    var weight by remember { mutableStateOf(profile.weightKg.toString()) }
    var gender by remember { mutableStateOf(profile.gender) }
    var stepGoal by remember { mutableStateOf(profile.stepGoal.toString()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)", color = TextSecondary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            label = { Text("Gender", color = TextSecondary) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = stepGoal,
            onValueChange = { stepGoal = it },
            label = { Text("Step Goal", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = TextSecondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = {
                    onSave(profile.copy(
                        name = name,
                        heightCm = height.toFloatOrNull() ?: 0f,
                        weightKg = weight.toFloatOrNull() ?: 0f,
                        gender = gender,
                        stepGoal = stepGoal.toIntOrNull() ?: 10000
                    ))
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("Save", color = Color.White)
            }
        }
    }
}
