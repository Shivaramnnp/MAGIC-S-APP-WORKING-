package com.shivasruthi.magics.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.shivasruthi.magics.ui.HomeScreen
import com.shivasruthi.magics.ui.ProcessingScreen
import com.shivasruthi.magics.ui.SummaryScreen
import com.shivasruthi.magics.viewmodel.EditorViewModel
// --- THE FIX: Add this import statement ---
import com.shivasruthi.magics.viewmodel.ProcessingViewModel
import com.shivasruthi.magics.viewmodel.SharedViewModel

@Composable
fun CreationNavGraph(sharedViewModel: SharedViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("processing/{uri}") { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri")
            val processingViewModel: ProcessingViewModel = viewModel()
            ProcessingScreen(
                navController = navController,
                uriString = uriString,
                viewModel = processingViewModel,
                sharedViewModel = sharedViewModel
            )
        }
        composable("summary") {
            SummaryScreen(
                navController = navController,
                sharedViewModel = sharedViewModel
            )
        }
        composable("editor") {
            val editorViewModel: EditorViewModel = viewModel()
            EditorScreen(
                navController = navController,
                viewModel = editorViewModel,
                sharedViewModel = sharedViewModel
            )
        }
    }
}