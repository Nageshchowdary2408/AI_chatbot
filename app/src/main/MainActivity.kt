package com.example.aichatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

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

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@Composable
fun ChatbotApp() {

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                "Hello! 👋 I am your AI Assistant. How can I help you?",
                false
            )
        )
    }

    var userInput by remember {
        mutableStateOf("")
    }

    var isTyping by remember {
        mutableStateOf(false)
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {

        ChatHeader()

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            items(messages) { message ->

                MessageBubble(
                    message = message
                )
            }

            if (isTyping) {

                item {
                    TypingIndicator()
                }
            }
        }

        ChatInput(
            message = userInput,
            onMessageChange = {
                userInput = it
            },
            onSendClick = {

                if (userInput.isNotBlank()) {

                    val question = userInput

                    messages.add(
                        ChatMessage(
                            text = question,
                            isUser = true
                        )
                    )

                    userInput = ""

                    isTyping = true

                    coroutineScope.launch {

                        delay(1000)

                        isTyping = false

                        val reply =
                            generateBotReply(question)

                        messages.add(
                            ChatMessage(
                                text = reply,
                                isUser = false
                            )
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun ChatHeader() {

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2563EB)
        ),
        shape = RoundedCornerShape(
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        )
    ) {

        Row(
            modifier = Modifier
                .padding(
                    horizontal = 20.dp,
                    vertical = 18.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "🤖",
                fontSize = 35.sp
            )

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column {

                Text(
                    text = "AI Assistant",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Online",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement =
            if (message.isUser) {
                Arrangement.End
            } else {
                Arrangement.Start
            }
    ) {

        Card(
            colors = CardDefaults.cardColors(
                containerColor =
                    if (message.isUser) {
                        Color(0xFF2563EB)
                    } else {
                        Color.White
                    }
            ),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            )
        ) {

            Text(
                text = message.text,
                modifier = Modifier
                    .padding(14.dp),
                color =
                    if (message.isUser) {
                        Color.White
                    } else {
                        Color.Black
                    },
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        CircularProgressIndicator(
            modifier = Modifier
                .width(22.dp)
                .height(22.dp),
            strokeWidth = 2.dp
        )

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = "AI is typing..."
        )
    }
}

@Composable
fun ChatInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .weight(1f),
            placeholder = {
                Text(
                    "Type a message..."
                )
            },
            shape = RoundedCornerShape(25.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Button(
            onClick = onSendClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2563EB)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {

            Text(
                text = "Send"
            )
        }
    }
}

fun generateBotReply(
    userMessage: String
): String {

    val message =
        userMessage.lowercase()

    return when {

        "hello" in message || "hi" in message -> {
            "Hello! 😊 How can I help you today?"
        }

        "what is android studio" in message -> {
            "Android Studio is the official development environment used to create Android applications."
        }

        "what is android" in message -> {
            "Android is an operating system mainly used in smartphones, tablets, TVs, and other devices."
        }

        "language" in message && "android" in message -> {
            "Kotlin and Java are commonly used for Android development. Kotlin is the recommended modern language."
        }

        "kotlin" in message -> {
            "Kotlin is a modern programming language used to develop Android applications."
        }

        "apk" in message -> {
            "APK stands for Android Package Kit. It is the installable file format for Android applications."
        }

        "activity" in message -> {
            "An Activity represents a screen in an Android application."
        }

        "jetpack compose" in message -> {
            "Jetpack Compose is a modern toolkit for creating Android user interfaces using Kotlin code."
        }

        "emulator" in message -> {
            "An Android emulator is a virtual Android device used to test applications on a computer."
        }

        "how" in message && "run" in message && "app" in message -> {
            "Select an emulator or connected phone and click the Run ▶ button in Android Studio."
        }

        "xml" in message -> {
            "XML is a markup language traditionally used to design Android application layouts."
        }

        "gradle" in message -> {
            "Gradle is the build system that manages dependencies and builds Android applications."
        }

        "sdk" in message -> {
            "The Android SDK contains tools and libraries required to develop Android applications."
        }

        "name" in message -> {
            "I am an AI chatbot created using Kotlin and Jetpack Compose."
        }

        "project" in message -> {
            "I can help you with Android, AI, machine learning, and B.Tech project ideas."
        }

        "thank" in message -> {
            "You are welcome! 😊"
        }

        "bye" in message -> {
            "Goodbye! Have a great day! 👋"
        }

        else -> {
            "I understand your message: \"$userMessage\". This is a demo chatbot. You can connect it to an AI API to generate intelligent answers."
        }
    }
}
