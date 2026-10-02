package com.example.aichatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================================
// 1. GET YOUR API KEY FROM https://aistudio.google.com/app/apikey
// 2. PASTE YOUR KEY BELOW BETWEEN THE QUOTES
const val MY_GEMINI_API_KEY = "PASTE_YOUR_API_KEY_HERE"
// ==========================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChatbotApp()
            }
        }
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)

@Composable
fun ChatbotApp() {
    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = MY_GEMINI_API_KEY
        )
    }

    val messages = remember {
        mutableStateListOf(
            ChatMessage("Hello! 👋 I'm your AI Assistant. How can I help you today?", false)
        )
    }

    var userInput by remember { mutableStateOf("") }
    var isTyping by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        ChatHeader()

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message -> MessageBubble(message) }
            if (isTyping) { item { TypingIndicator() } }
        }

        ChatInput(
            message = userInput,
            onMessageChange = { userInput = it },
            onSendClick = {
                if (userInput.isNotBlank()) {
                    val question = userInput
                    messages.add(ChatMessage(question, true))
                    userInput = ""

                    // Safety check for empty key
                    if (MY_GEMINI_API_KEY == "PASTE_YOUR_API_KEY_HERE" || MY_GEMINI_API_KEY.isBlank()) {
                        messages.add(ChatMessage("⚠️ API Key not found! Please paste your key at the top of MainActivity.kt to get AI answers.", false))
                        return@ChatInput
                    }

                    isTyping = true
                    coroutineScope.launch {
                        try {
                            // Check local "trained" knowledge base first
                            val localReply = generateLocalReply(question)
                            
                            val finalReply = if (localReply != null) {
                                delay(500) // Small delay to feel natural
                                localReply
                            } else {
                                // Connect to Gemini AI for intelligent answers
                                val response = generativeModel.generateContent(question)
                                response.text ?: "I'm sorry, I couldn't generate a response."
                            }

                            isTyping = false
                            messages.add(ChatMessage(finalReply, false))
                        } catch (e: Exception) {
                            isTyping = false
                            messages.add(ChatMessage("Error: ${e.localizedMessage}. Check your key or connection.", false))
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ChatHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2563EB)),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "🤖", fontSize = 35.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "AI Intelligence", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text(text = "Online | Powered by Gemini", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = if (message.isUser) Color(0xFF2563EB) else Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(text = message.text, modifier = Modifier.padding(14.dp), color = if (message.isUser) Color.White else Color.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF2563EB))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "AI is thinking...", color = Color.Gray, fontSize = 14.sp)
    }
}

@Composable
fun ChatInput(message: String, onMessageChange: (String) -> Unit, onSendClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask anything...") },
            shape = RoundedCornerShape(25.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSendClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)), shape = RoundedCornerShape(20.dp)) {
            Text("Send")
        }
    }
}

fun generateLocalReply(userMessage: String): String? {
    val message = userMessage.lowercase()
    return when {
        "hello" in message || "hi" in message -> "Hello! 😊 How can I help you today?"
        "android studio" in message -> "Android Studio is the official development environment used to create Android applications."
        "what is android" in message -> "Android is an operating system mainly used in smartphones, tablets, TVs, and other devices."
        "kotlin" in message -> "Kotlin is a modern programming language used to develop Android applications."
        "apk" in message -> "APK stands for Android Package Kit. It is the installable file format for Android applications."
        "activity" in message -> "An Activity represents a screen in an Android application."
        "jetpack compose" in message -> "Jetpack Compose is a modern toolkit for creating Android user interfaces using Kotlin code."
        "project" in message -> "I can help you with Android, AI, machine learning, and B.Tech project ideas."
        else -> null 
    }
}
