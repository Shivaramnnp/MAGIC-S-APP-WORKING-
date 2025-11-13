package com.shivasruthi.magics.ui

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.shivasruthi.magics.viewmodel.ProcessingUiState
import com.shivasruthi.magics.viewmodel.ProcessingViewModel
import com.shivasruthi.magics.viewmodel.SharedViewModel
import kotlinx.coroutines.delay

// --- THE FIX: A list of engaging quotes ---
private val motivationalQuotes = listOf(
//    "I adore you, Sruthi ❤️",
//    "Forever yours, Sruthi 💕",
//    "Sruthi, my heart belongs to you 💖",
//    "Always with you, Sruthi 🌹",
//    "You are my everything, Sruthi 💞",
//    "Sruthi, my love for you never ends 💓",
//    "Sruthi, you complete my world 💗",
//    "My heart beats for you, Sruthi 💝",
//    "Sruthi, you are my sunshine ☀️",
//    "My soul loves you, Sruthi 💫",
//    "Sruthi, my endless love 💍",
//    "You make my life beautiful, Sruthi 🌸",
//    "Sruthi, you are my dream come true ✨",
//    "I’m lost in you, Sruthi 🌀",
//    "Sruthi, my forever and always 💌",
//    "You are my smile, Sruthi 😊",
//    "My heart chose you, Sruthi 💘",
//    "Sruthi, my sweet angel 👼",
//    "With you, life is perfect Sruthi 🌈",
//    "Sruthi, you are my destiny 🌟",
//    "My love grows for you daily, Sruthi 🌱",
//    "Sruthi, my heart’s queen 👑",
//    "I breathe for you, Sruthi 🌬️",
//    "Sruthi, you are my safe place 🏡",
//    "I’m nothing without you, Sruthi 🖤",
//    "Sruthi, my heart dances for you 💃",
//    "You are my best part, Sruthi 🌹",
//    "Sruthi, my love story 💕",
//    "Life means you, Sruthi 🌍",
//    "Sruthi, my reason to smile 😍",
//    "You and me forever, Sruthi ♾️",
//    "Sruthi, my moon and stars 🌙",
//    "I’m incomplete without you, Sruthi 🧩",
//    "Sruthi, my one in a million 💎",
//    "My world is you, Sruthi 🌏",
//    "Sruthi, my sweetest addiction 🍯",
//    "You hold my heart, Sruthi 🔐",
//    "Sruthi, my eternal flame 🔥",
//    "My heart beats your name, Sruthi ❤️",
//    "Sruthi, my precious soul 🌼",
//    "You are my today and tomorrow, Sruthi 📅",
//    "Sruthi, my purest love 💧",
//    "I’m forever yours, Sruthi 🥰",
//    "Sruthi, my happy place 🌻",
//    "You are my everything and more, Sruthi 💞",
//    "Sruthi, my sweetest blessing 🙏",
//    "I will always choose you, Sruthi 💝",
//    "Sruthi, my heartbeat’s rhythm 🎶",
//    "You are my forever love, Sruthi 💗",

    "Assembling your path to success...",
    "Every mock test is a step towards your goal.",
    "Transforming your notes into a powerful study tool...",
    "The more you practice, the luckier you get.",
    "Identifying key concepts for your review...",
    "Confidence is built on preparation. Let's get you prepared.",
    "Analyzing your document to find every advantage...",
    "Don't wish for it. Work for it. This is part of the work.",
    "Building your custom practice exam now...",
    "Success is the sum of small efforts, repeated day in and day out.",
    "Get ready to challenge yourself and conquer the material.",
    "Finalizing your secret weapon for exam day..."
)

@Composable
fun ProcessingScreen(
    navController: NavController,
    uriString: String?,
    viewModel: ProcessingViewModel,
    sharedViewModel: SharedViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // --- THE FIX: State for the currently displayed quote ---
    var currentQuoteIndex by remember { mutableStateOf(0) }

    fun startProcessing() {
        uriString?.let {
            val uri = Uri.parse(it)
            context.contentResolver.openFileDescriptor(uri, "r")?.let { pfd ->
                viewModel.startPdfProcessing(pfd, context.applicationContext)
            }
        }
    }

    LaunchedEffect(Unit) {
        startProcessing()
    }

    // --- THE FIX: A coroutine to cycle through quotes ---
    LaunchedEffect(uiState) {
        // Only run the animation while in the Loading state
        if (uiState is ProcessingUiState.Loading) {
            while (true) {
                delay(2000) // Wait for 3 seconds
                currentQuoteIndex = (currentQuoteIndex + 1) % motivationalQuotes.size
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val state = uiState) {
            is ProcessingUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Analyzing Your Document",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // --- THE FIX: Animated text for the quotes ---
                AnimatedContent(
                    targetState = currentQuoteIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    }, label = "quote animation"
                ) { index ->
                    Text(
                        text = motivationalQuotes[index],
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is ProcessingUiState.Success -> {
                LaunchedEffect(state.questions) {
                    sharedViewModel.setQuestions(state.questions)
                    navController.navigate("summary") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            }
            is ProcessingUiState.Error -> {
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { startProcessing() }) {
                    Text("Try Again")
                }
            }
        }
    }
}