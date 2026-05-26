package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.ChatMessage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(
    messages: List<ChatMessage>,
    isTyping: Boolean,
    onSendMessage: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✨ VitaAI Coach", color = TextPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundSecondary
            )
        )

        // Health context badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundTertiary)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("✅ Coach has access to: Steps · Sleep · HR", color = Teal, style = MaterialTheme.typography.labelSmall)
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = false
        ) {
            items(messages) { msg ->
                MessageBubble(msg)
            }
            if (isTyping) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Input
        Surface(
            color = BackgroundSecondary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Ask about your health...", color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundTertiary,
                        unfocusedContainerColor = BackgroundTertiary,
                        focusedBorderColor = Teal,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            onSendMessage(text)
                            text = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Teal)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = BackgroundPrimary)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage) {
    val isUser = msg.role == "user"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = if (isUser) 24.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 24.dp
                ))
                .background(if (isUser) Teal.copy(alpha = 0.15f) else BackgroundSecondary)
                .border(1.dp, if (isUser) Teal.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(
                    topStart = 24.dp,
                    topEnd = 24.dp,
                    bottomStart = if (isUser) 24.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 24.dp
                ))
                .padding(16.dp)
        ) {
            Text(
                text = msg.content,
                color = if (isUser) Teal else TextPrimary
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Text("✨ Coach is thinking...", color = Teal, modifier = Modifier.padding(16.dp))
}
