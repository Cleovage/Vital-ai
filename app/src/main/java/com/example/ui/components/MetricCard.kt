package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap

import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.zIndex

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    progress: Float = 0f, // 0 to 1
    badgeText: String = "No data",
    progressColor: Color = AccentTeal,
    modifier: Modifier = Modifier,
    onQuickAdd: (() -> Unit)? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundSecondary)
            .clickable { onClick() }
            .padding(16.dp)
            .aspectRatio(0.85f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().zIndex(1f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                if (onQuickAdd != null) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(progressColor.copy(alpha = 0.2f), CircleShape)
                            .clip(CircleShape)
                            .clickable { onQuickAdd() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = progressColor, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium
                )
                if (unit.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Dummy chart (dots and days)
            if (value != "No data" && progress > 0f) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Just draw some dummy bars
                    val days = listOf("W", "T", "F", "S", "S", "M", "T")
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom, modifier = Modifier.fillMaxHeight()) {
                            if (index == 6) { // today
                                Box(modifier = Modifier.width(12.dp).height((40 * progress).dp).background(progressColor, shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = day, color = if (index == 6) TextPrimary else TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(), 
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val days = listOf("W", "T", "F", "S", "S", "M", "T")
                    days.forEachIndexed { index, day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(6.dp).background(TextMuted, CircleShape))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = day, color = if (index == 6) TextPrimary else TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Badge
            Box(
                modifier = Modifier
                    .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(text = badgeText, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
