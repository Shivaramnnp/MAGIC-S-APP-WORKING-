package com.shivasruthi.magics.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.shivasruthi.magics.viewmodel.SharedViewModel

@Composable
fun SummaryScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel
) {
    val questions by sharedViewModel.questions.collectAsState()

    val totalQuestions = questions.size
    val correctQuestions = questions.count { it.correctAnswerIndex != -1 }
    val needsEditing = totalQuestions - correctQuestions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Scan Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(32.dp))

        SummaryInfoCard(
            icon = Icons.Default.ListAlt,
            label = "Total Questions Found",
            count = totalQuestions,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        SummaryInfoCard(
            icon = Icons.Default.CheckCircle,
            label = "Ready to Use",
            count = correctQuestions,
            color = Color(0xFF00C853) // Green
        )
        Spacer(modifier = Modifier.height(16.dp))
        SummaryInfoCard(
            icon = Icons.Default.Edit,
            label = "Requires Editing",
            count = needsEditing,
            color = MaterialTheme.colorScheme.error // Red
        )

        Spacer(modifier = Modifier.weight(1f))

        // Primary Button
        Button(
            onClick = { navController.navigate("editor") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Review and Edit Questions", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- THE DEFINITIVE FIX: The "Scan Again" button ---
        OutlinedButton(
            onClick = {
                // Navigate back to the home screen and clear the history
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Scan Another Document", fontSize = 16.sp)
        }
    }
}

@Composable
fun SummaryInfoCard(icon: ImageVector, label: String, count: Int, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = label, tint = color)
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}