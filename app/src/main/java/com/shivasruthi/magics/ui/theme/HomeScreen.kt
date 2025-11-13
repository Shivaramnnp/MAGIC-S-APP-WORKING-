package com.shivasruthi.magics.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun HomeScreen(navController: NavController) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val encodedUri = URLEncoder.encode(it.toString(), StandardCharsets.UTF_8.toString())
            navController.navigate("processing/$encodedUri")
        }
    }

    // Use a Box to allow for more flexible alignment
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp) // Add overall padding
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. A prominent icon to give the app a visual identity
            Icon(
                imageVector = Icons.Default.DocumentScanner,
                contentDescription = "Scan Document Icon",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. A clear title and subtitle
            Text(
                text = "Magic S",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Turn any document into an interactive mock test instantly.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // 3. A well-structured set of action buttons at the bottom
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Primary Action: A large, filled button
            Button(
                onClick = { launcher.launch("*/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add File Icon",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Scan PDF or Image")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Action: A less prominent outlined button
            OutlinedButton(
                onClick = { navController.navigate("editor") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Manual Entry")
            }
        }
    }
}